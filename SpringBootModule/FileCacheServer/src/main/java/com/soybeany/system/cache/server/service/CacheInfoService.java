package com.soybeany.system.cache.server.service;

import com.soybeany.cache.v2.contract.IDatasource;
import com.soybeany.cache.v2.core.DataManager;
import com.soybeany.cache.v2.exception.NoDataSourceException;
import com.soybeany.cache.v2.log.StdLogger;
import com.soybeany.cache.v2.model.DataPack;
import com.soybeany.download.core.FileInfo;
import com.soybeany.system.cache.core.dto.FileUid;
import com.soybeany.system.cache.server.config.AppConfig;
import com.soybeany.system.cache.server.model.CacheLogWriter;
import com.soybeany.system.cache.server.model.DataInfo;
import com.soybeany.system.cache.server.storage.FileCacheAccessor;
import com.soybeany.system.cache.server.storage.FileCacheStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.util.Optional;

/**
 * @author Soybeany
 * @date 2020/12/4
 */
@Service
public class CacheInfoService {

    @Autowired
    private AppConfig appConfig;
    @Autowired
    private DownloadService downloadService;

    private FileCacheStorage cacheStorage;
    private DataManager<FileUid, FileCacheAccessor> dataManager;

    public <T> T receiveCacheInfo(FileUid fileUid, IListener<T> listener) throws Exception {
        FileCacheAccessor accessor = dataManager.getData(fileUid);
        DataInfo dataInfo = accessor.dataInfo;
        long contentLength = Optional.ofNullable(dataInfo.contentLength).orElseGet(() -> accessor.file().length());
        FileInfo fileInfo = new FileInfo(dataInfo.contentDisposition, contentLength, dataInfo.eTag);
        return listener.onReceiveCacheInfo(fileInfo.contentType(dataInfo.contentType), accessor.file());
    }

    public boolean isCacheExist(FileUid fileUid) {
        DataPack<FileCacheAccessor> dataPack = dataManager.getDataPack(fileUid, null);
        try {
            dataPack.getData();
            return true;
        } catch (NoDataSourceException e) {
            return false;
        }
    }

    @PostConstruct
    private void onInit() {
        cacheStorage = new FileCacheStorage(appConfig.fileCacheDir);
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

    public interface IListener<T> {
        T onReceiveCacheInfo(FileInfo fileInfo, File file) throws Exception;
    }

    private class Datasource implements IDatasource<FileUid, FileCacheAccessor> {
        @Override
        public FileCacheAccessor onGetData(FileUid fileUid) {
            DownloadService.DownloadInfo info = downloadService.startDownload(fileUid);
            return FileCacheAccessor.fromStream(info.info, () -> info.is);
        }

        @Override
        public int onSetupExpiry(FileCacheAccessor fileCacheAccessor) {
            return fileCacheAccessor.dataInfo.pTtl;
        }
    }
}
