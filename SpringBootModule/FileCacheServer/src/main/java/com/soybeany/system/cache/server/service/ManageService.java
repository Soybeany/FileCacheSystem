package com.soybeany.system.cache.server.service;

import com.soybeany.cache.v2.contract.IDatasource;
import com.soybeany.cache.v2.core.DataManager;
import com.soybeany.cache.v2.exception.NoDataSourceException;
import com.soybeany.cache.v2.log.StdLogger;
import com.soybeany.cache.v2.model.DataPack;
import com.soybeany.download.FileServerUtils;
import com.soybeany.download.core.FileInfo;
import com.soybeany.system.cache.core.dto.FileUid;
import com.soybeany.system.cache.core.util.ExInfoUtils;
import com.soybeany.system.cache.core.util.LogUtils;
import com.soybeany.system.cache.server.config.AppConfig;
import com.soybeany.system.cache.server.config.IDynamicConfigProvider;
import com.soybeany.system.cache.server.model.CacheLogWriter;
import com.soybeany.system.cache.server.model.DataInfo;
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
import java.util.Optional;
import java.util.UUID;

/**
 * @author Soybeany
 * @date 2020/12/4
 */
@Service
public class ManageService implements ICacheProvider {

    private static final Logger LOG = LoggerFactory.getLogger(ManageService.class);

    @Autowired
    private AppConfig appConfig;
    @Autowired
    private DownloadService downloadService;
    @Autowired
    private IDynamicConfigProvider configProvider;

    private FileCacheStorage cacheStorage;
    private DataManager<FileUid, FileCacheAccessor> dataManager;

    @Override
    public void onDownload(String token, HttpServletRequest request, HttpServletResponse response) {
        handleContentInfo(token, response, (dataInfo, file) -> {
            // 自定义header设置
            if (null != dataInfo.exInfo) {
                response.setHeader(ExInfoUtils.HEADER_EX_INFO, ExInfoUtils.encodeExInfo(dataInfo.exInfo));
            }
            // 数据下载
            long contentLength = Optional.ofNullable(dataInfo.contentLength).orElseGet(file::length);
            FileInfo fileInfo = new FileInfo(dataInfo.contentDisposition, contentLength, dataInfo.eTag);
            FileServerUtils.randomAccessDownloadFile(fileInfo.contentType(dataInfo.contentType), request, response, file);
            return null;
        }, e -> null);
    }

    @Override
    public String onEnsure(String token, HttpServletRequest request, HttpServletResponse response) {
        return handleContentInfo(token, response, (dataInfo, file) -> "ok", e -> "exception");
    }

    public <T> T retrieveCache(FileUid fileUid, ICallback<T> callback) throws Exception {
        FileCacheAccessor.Local accessor = (FileCacheAccessor.Local) dataManager.getData(fileUid);
        return callback.onHandle(accessor.dataInfo, accessor.file());
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

    protected String toErrMsg(Exception e) {
        return LogUtils.exceptionToString(e);
    }

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

    private <T> T handleContentInfo(String token, HttpServletResponse response, ManageService.ICallback<T> callback, IExceptionHandler<T> handler) {
        try {
            FileUid fileUid = configProvider.toFileUid(token);
            return retrieveCache(fileUid, callback);
        } catch (Exception e) {
            String uuid = UUID.randomUUID().toString();
            LOG.error(uuid + " - " + toErrMsg(e));
            if (response.isCommitted()) {
                return null;
            }
            response.setHeader("errMsg", uuid);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return handler.onHandleException(e);
        }
    }

    // ***********************内部类****************************

    public interface ICallback<T> {
        T onHandle(DataInfo dataInfo, File file) throws Exception;
    }

    private interface IExceptionHandler<T> {
        T onHandleException(Exception e);
    }

    private class Datasource implements IDatasource<FileUid, FileCacheAccessor> {
        @Override
        public FileCacheAccessor onGetData(FileUid fileUid) {
            return downloadService.startDownload(fileUid);
        }

        @Override
        public int onSetupExpiry(FileCacheAccessor fileCacheAccessor) {
            return fileCacheAccessor.dataInfo.pTtl;
        }

        @Override
        public int onSetupExpiry(Exception e) {
            if (e instanceof ReDownloadException) {
                return 100;
            }
            return IDatasource.super.onSetupExpiry(e);
        }
    }
}
