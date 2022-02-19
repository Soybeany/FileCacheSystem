package com.soybeany.system.cache.server.service;

import com.soybeany.download.FileClientUtils;
import com.soybeany.download.core.DownloadConfig;
import com.soybeany.download.core.FileInfo;
import com.soybeany.download.core.TempFileInfo;
import com.soybeany.rpc.consumer.api.IRpcServiceProxy;
import com.soybeany.rpc.consumer.model.RpcProxySelector;
import com.soybeany.system.cache.core.api.FileCacheContract;
import com.soybeany.system.cache.core.api.ICacheAppInfoProvider;
import com.soybeany.system.cache.core.model.CacheAppInfo;
import com.soybeany.system.cache.core.model.FileUid;
import com.soybeany.system.cache.server.exception.FileDownloadException;
import com.soybeany.system.cache.server.exception.FileStorageException;
import com.soybeany.system.cache.server.model.CacheFileInfo;
import com.soybeany.system.cache.server.model.TempFileInfoP;
import com.soybeany.system.cache.server.repository.DbDAO;
import com.soybeany.system.cache.server.repository.TempFileRepository;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

/**
 * @author Soybeany
 * @date 2022/2/16
 */
@Slf4j
@Service
public class FileDownloadServiceImpl implements FileDownloadService {

    private static final int DEFAULT_AGE = 30 * 24 * 60 * 60;

    @Autowired
    private ConfigService configService;
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private DbDAO dbDAO;
    @Autowired
    private TempFileRepository tempFileRepository;
    @Autowired
    private IRpcServiceProxy serviceProxy;

    private RpcProxySelector<ICacheAppInfoProvider> providerSelector;

    @Override
    public CacheFileInfo downloadFile(FileUid fileUid) throws FileDownloadException {
        try {
            return downloadFile(fileUid, getDownloadConfig(fileUid), getTempFileInfo(fileUid));
        } catch (Exception e) {
            throw new FileDownloadException(e.getMessage());
        }
    }

    // ***********************内部方法****************************

    private DownloadConfig getDownloadConfig(FileUid fileUid) {
        CacheAppInfo info = providerSelector.get(fileUid.server).getInfo();
        return new DownloadConfig(info.getCompleteUrl(fileUid.fileToken))
                .header(FileCacheContract.AUTHORIZATION, info.authorization())
                .timeout(configService.get().getDownloadTimeoutSec());
    }

    private TempFileInfo getTempFileInfo(FileUid fileUid) {
        return getTempFileInfoP(fileUid)
                .map(info -> new TempFileInfo(fileStorageService.loadTempFile(fileUid, info.tempFileName), info.eTag))
                .orElseGet(() -> TempFileInfo.getNew(fileStorageService.getTempFileDir()));
    }

    private Optional<TempFileInfoP> getTempFileInfoP(FileUid fileUid) {
        return tempFileRepository.findByFileUid(FileUid.toString(fileUid));
    }

    private CacheFileInfo downloadFile(FileUid fileUid, DownloadConfig config, TempFileInfo tempFileInfo) throws Exception {
        Result result = new Result();
        CountDownLatch latch = new CountDownLatch(1);
        FileClientUtils.downloadFile(config, tempFileInfo, new FileClientUtils.ICallback() {
            private boolean isSuccess;

            @Override
            public void onSuccess(Response response, FileInfo info, File tempFile) {
                long contentLength = info.contentLength();
                if (contentLength > 0 && contentLength != tempFile.length()) {
                    result.beError(new Exception("数据下载不完整"));
                    deleteTempFileAndInfo(getTempFileInfoP(fileUid));
                    return;
                }
                try {
                    File file = fileStorageService.saveFile(fileUid, tempFile);
                    int age = Optional.ofNullable(response.header("Age")).map(Integer::parseInt).orElse(DEFAULT_AGE);
                    result.beNorm(new CacheFileInfo(info.contentDisposition(), contentLength, info.eTag(), age, file));
                    isSuccess = true;
                } catch (FileStorageException e) {
                    result.beError(e);
                }
            }

            @Override
            public void onFailure(Response response, FileInfo info, IOException e) {
                result.beError(e);
            }

            @Override
            public void onFinal(Response response, FileInfo info) {
                handleTempFile(info);
                latch.countDown();
            }

            private void handleTempFile(FileInfo info) {
                Optional<TempFileInfoP> tempFileInfoP = getTempFileInfoP(fileUid);
                String fileUidStr = FileUid.toString(fileUid);
                File tempFile = tempFileInfo.getTempFile();
                // 成功则删除临时记录与临时文件
                if (isSuccess) {
                    log.info("文件“" + fileUidStr + "”下载完成");
                    deleteTempFileAndInfo(tempFileInfoP);
                    return;
                }
                // 否则更新临时文件信息
                TempFileInfoP tfi = tempFileInfoP.orElseGet(() -> {
                    TempFileInfoP newOne = new TempFileInfoP();
                    newOne.fileUid = FileUid.toString(fileUid);
                    newOne.tempFileName = tempFile.getName();
                    return newOne;
                });
                tfi.eTag = info.eTag();
                tfi.downloadedLength = tempFile.length();
                tfi.totalLength = info.contentLength();
                dbDAO.saveTempFileInfo(tfi);
                log.info("文件“" + fileUidStr + "”部分已下载(" + tfi.downloadedLength + "/" + tfi.totalLength + ")，临时文件为“" + tempFile.getName());
            }

            private void deleteTempFileAndInfo(Optional<TempFileInfoP> tempFileInfoP) {
                tempFileInfoP.ifPresent(tfi -> dbDAO.deleteTempFileInfo(tfi));
                File tempFile = tempFileInfo.getTempFile();
                if (tempFile.exists()) {
                    boolean deleted = tempFile.delete();
                    log.info("临时文件“" + tempFile.getName() + "”删除" + (deleted ? "成功" : "失败"));
                }
            }

        });
        latch.await();
        if (result.isNorm) {
            return result.fileInfo;
        }
        throw result.e;
    }

    @PostConstruct
    private void onInit() {
        providerSelector = serviceProxy.getSelector(ICacheAppInfoProvider.class);
    }

    // ***********************内部类****************************

    private static class Result {
        boolean isNorm;
        CacheFileInfo fileInfo;
        Exception e;

        void beNorm(CacheFileInfo file) {
            this.isNorm = true;
            this.fileInfo = file;
        }

        void beError(Exception e) {
            this.isNorm = false;
            this.e = e;
        }
    }

}
