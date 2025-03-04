package com.soybeany.system.cache.server.service;

import com.soybeany.cache.v2.contract.ICacheStorage;
import com.soybeany.cache.v2.contract.IDatasource;
import com.soybeany.cache.v2.core.DataManager;
import com.soybeany.cache.v2.exception.NoDataSourceException;
import com.soybeany.cache.v2.log.StdLogger;
import com.soybeany.cache.v2.model.DataPack;
import com.soybeany.download.DataSupplier;
import com.soybeany.download.core.Md5Type;
import com.soybeany.system.cache.core.dto.FileUid;
import com.soybeany.system.cache.core.security.interfaces.FileCacheHttpContract;
import com.soybeany.system.cache.core.security.model.FcException;
import com.soybeany.system.cache.core.util.LogUtils;
import com.soybeany.system.cache.server.config.AppConfig;
import com.soybeany.system.cache.server.config.IDynamicConfigProvider;
import com.soybeany.system.cache.server.model.*;
import com.soybeany.system.cache.server.storage.FileCacheAccessor;
import com.soybeany.system.cache.server.storage.FileCacheStorage;
import com.soybeany.util.ExceptionUtils;
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
import java.util.*;
import java.util.function.Supplier;

/**
 * @author Soybeany
 * @date 2020/12/4
 */
@Service
public class CacheService {

    private static final String HEADER_DATA_FROM = "x-data-from";
    private static final Logger LOG = LoggerFactory.getLogger(CacheService.class);

    private final Map<String, Set<String>> downloadingMap = new HashMap<>();

    @Autowired
    private AppConfig appConfig;
    @Autowired
    private DownloadService downloadService;
    @Autowired
    private IDynamicConfigProvider configProvider;

    private FileCacheStorage cacheStorage;
    private DataManager<FileUid, FileCacheAccessor> dataManager;

    // ***********************外部API****************************

    public Map<String, Set<String>> getDownloadingMap() {
        Map<String, Set<String>> result = new HashMap<>();
        downloadingMap.forEach((k, v) -> result.put(k, new HashSet<>(v)));
        return result;
    }

    public Map<String, MetaInfo> getCompletedMap(FileUid... fileUids) {
        if (null == fileUids) {
            return Collections.emptyMap();
        }
        return cacheStorage.getMetaInfo(fileUids);
    }

    public DiscSpaceInfo getDiscSpaceInfo() {
        return cacheStorage.getDiscSpaceInfo();
    }

    public void removeCache(FileUid... fileUids) {
        if (null == fileUids) {
            return;
        }
        for (FileUid fileUid : fileUids) {
            dataManager.removeCache(fileUid);
        }
    }

    public void download(String token, HttpServletRequest request, HttpServletResponse response) {
        handleContentInfo("下载", token, request, response, (from, dataInfo, file) -> {
            // 自定义header设置
            if (null != dataInfo.exInfo) {
                response.setHeader(FileCacheHttpContract.HEADER_EX_INFO, FileCacheHttpContract.encodeExInfo(dataInfo.exInfo));
            }
            response.setHeader(HEADER_DATA_FROM, getFromDesc(from));
            // 数据下载
            try {
                DataSupplier.builder()
                        .contentDisposition(dataInfo.contentDisposition)
                        .contentLength(dataInfo.contentLength)
                        .dataFrom(file, onSetupMd5Type())
                        .contentType(dataInfo.contentType)
                        .eTag(dataInfo.eTag)
                        .enableRandomAccess(request)
                        .start(response);
            } catch (Exception e) {
                throw new FcException("下载异常:" + ExceptionUtils.getExceptionDetail(e));
            }
        });
    }

    public void retrieveCache(String desc, FileUid fileUid, ICallback callback) {
        DataPack<FileCacheAccessor> dataPack = time("缓存", () -> dataManager.getDataPack(fileUid));
        FileCacheAccessor.Local accessor = (FileCacheAccessor.Local) dataPack.getData();
        time(desc, () -> {
            callback.onHandle(dataPack.provider, accessor.dataInfo, accessor.file());
            return null;
        });
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

    private <T> T time(String desc, Supplier<T> action) {
        LOG.info(desc + "开始");
        long startTime = System.currentTimeMillis();
        try {
            return action.get();
        } finally {
            LOG.info(desc + "结束，耗时:" + (System.currentTimeMillis() - startTime) + "ms");
        }
    }

    protected Md5Type onSetupMd5Type() {
        return Md5Type.STD_PARTIAL;
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
                .get("文件缓存", new Datasource(), FileUid::getKey)
                .withCache(cacheStorage)
                .logger(new StdLogger<>(new CacheLogWriter()))
                .build();
        cacheStorage.start();
    }

    @PreDestroy
    private void onDestroy() {
        cacheStorage.close();
    }

    private String getFromDesc(Object from) {
        if (from instanceof ICacheStorage) {
            return "cache";
        } else {
            return from instanceof IDatasource ? "source" : "other(" + from + ")";
        }
    }

    private void handleContentInfo(String desc, String token, HttpServletRequest request, HttpServletResponse response, CacheService.ICallback callback) {
        try {
            FileUid fileUid = configProvider.toFileUid(token);
            fileUid.exInfo = FileCacheHttpContract.decodeExInfo(request.getHeader(FileCacheHttpContract.HEADER_EX_INFO));
            retrieveCache(desc, fileUid, callback);
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
        void onHandle(Object from, DataInfo dataInfo, File file);
    }

    private class Datasource implements IDatasource<FileUid, FileCacheAccessor> {
        @Override
        public FileCacheAccessor onGetData(FileUid fileUid) {
            try {
                synchronized (downloadingMap) {
                    downloadingMap.computeIfAbsent(fileUid.server, k -> new HashSet<>()).add(fileUid.fileId);
                }
                return downloadService.startDownload(fileUid);
            } finally {
                synchronized (downloadingMap) {
                    Optional.ofNullable(downloadingMap.get(fileUid.server)).ifPresent(m -> m.remove(fileUid.fileId));
                }
            }
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
