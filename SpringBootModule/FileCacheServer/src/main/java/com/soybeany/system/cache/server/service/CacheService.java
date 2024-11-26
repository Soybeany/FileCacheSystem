package com.soybeany.system.cache.server.service;

import com.soybeany.cache.v2.contract.IDatasource;
import com.soybeany.cache.v2.core.DataManager;
import com.soybeany.cache.v2.exception.NoDataSourceException;
import com.soybeany.cache.v2.log.StdLogger;
import com.soybeany.cache.v2.model.DataPack;
import com.soybeany.download.FileServerUtils;
import com.soybeany.download.core.FileInfo;
import com.soybeany.system.cache.core.dto.FileUid;
import com.soybeany.system.cache.core.security.interfaces.FileCacheHttpContract;
import com.soybeany.system.cache.core.security.model.FcException;
import com.soybeany.system.cache.core.util.LogUtils;
import com.soybeany.system.cache.server.config.AppConfig;
import com.soybeany.system.cache.server.config.IDynamicConfigProvider;
import com.soybeany.system.cache.server.model.CacheLogWriter;
import com.soybeany.system.cache.server.model.DataInfo;
import com.soybeany.system.cache.server.model.ReDownloadException;
import com.soybeany.system.cache.server.storage.FileCacheAccessor;
import com.soybeany.system.cache.server.storage.FileCacheStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.net.URLEncoder;
import java.util.Optional;

/**
 * @author Soybeany
 * @date 2020/12/4
 */
@Service
public class CacheService {

    private static final Logger LOG = LoggerFactory.getLogger(CacheService.class);

    @Autowired
    private AppConfig appConfig;
    @Autowired
    private DownloadService downloadService;
    @Autowired
    private IDynamicConfigProvider configProvider;

    private FileCacheStorage cacheStorage;
    private DataManager<FileUid, FileCacheAccessor> dataManager;

    // ***********************外部API****************************

    public void download(String token, HttpServletRequest request, HttpServletResponse response) {
        handleContentInfo(token, request, response, (dataInfo, file) -> {
            // 自定义header设置
            if (null != dataInfo.exInfo) {
                response.setHeader(FileCacheHttpContract.HEADER_EX_INFO, FileCacheHttpContract.encodeExInfo(dataInfo.exInfo));
            }
            // 数据下载
            try {
                FileServerUtils.randomAccessDownloadFile(toFileInfo(dataInfo, file), request, response, file);
            } catch (Exception e) {
                throw new FcException(e);
            }
        });
    }

    public void retrieveCache(FileUid fileUid, ICallback callback) {
        FileCacheAccessor.Local accessor = (FileCacheAccessor.Local) dataManager.getData(fileUid);
        callback.onHandle(accessor.dataInfo, accessor.file());
    }

    @SuppressWarnings("unused")
    public boolean isCacheExist(FileUid fileUid) {
        DataPack<FileCacheAccessor> dataPack = dataManager.getDataPack(fileUid, null);
        try {
            dataPack.getData();
            return true;
        } catch (NoDataSourceException e) {
            return false;
        }
    }

    // ***********************子类重写****************************

    protected FileInfo toFileInfo(DataInfo dataInfo, File file) {
        long contentLength = Optional.ofNullable(dataInfo.contentLength).orElseGet(file::length);
        FileInfo fileInfo = new FileInfo(dataInfo.contentDisposition, contentLength, dataInfo.eTag);
        fileInfo.contentType(dataInfo.contentType);
        return fileInfo;
    }

    protected String toErrMsg(Exception e) {
        try {
            return URLEncoder.encode(e.getMessage(), "UTF-8");
        } catch (Exception e2) {
            return "please read logs";
        }
    }

    // ***********************内部方法****************************

    @PostConstruct
    private void onInit() {
        cacheStorage = new FileCacheStorage(appConfig.fileCacheDir, appConfig.maxUsedPercent);
        dataManager = DataManager.Builder
                .get("文件缓存", new Datasource(), id -> id.fileId)
                .withCache(cacheStorage)
                .logger(new StdLogger<>(new CacheLogWriter()))
                .build();
        cacheStorage.start();
    }

    @PreDestroy
    private void onDestroy() {
        cacheStorage.close();
    }

    private void handleContentInfo(String token, HttpServletRequest request, HttpServletResponse response, CacheService.ICallback callback) {
        try {
            FileUid fileUid = configProvider.toFileUid(token);
            fileUid.exInfo = FileCacheHttpContract.decodeExInfo(request.getHeader(FileCacheHttpContract.HEADER_EX_INFO));
            retrieveCache(fileUid, callback);
        } catch (Exception e) {
            LOG.error(LogUtils.exceptionToString(e));
            if (response.isCommitted()) {
                return;
            }
            response.setHeader(FileCacheHttpContract.HEADER_ERR_MSG, toErrMsg(e));
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    // ***********************内部类****************************

    public interface ICallback {
        void onHandle(DataInfo dataInfo, File file);
    }

    private class Datasource implements IDatasource<FileUid, FileCacheAccessor> {
        @Override
        public FileCacheAccessor onGetData(FileUid fileUid) {
            return downloadService.startDownload(fileUid);
        }

        @Override
        public long onSetupExpiry(FileCacheAccessor fileCacheAccessor) {
            return fileCacheAccessor.dataInfo.pTtl;
        }

        @Override
        public long onSetupExpiry(Exception e) {
            if (e instanceof ReDownloadException) {
                return 100;
            }
            return IDatasource.super.onSetupExpiry(e);
        }
    }
}
