package com.soybeany.system.cache.server.service;

import com.soybeany.download.core.FileInfo;
import com.soybeany.system.cache.core.model.FileUid;
import com.soybeany.system.cache.core.util.CacheCoreTimeUtils;
import com.soybeany.system.cache.server.config.AppConfig;
import com.soybeany.system.cache.server.model.CacheInfo;
import com.soybeany.system.cache.server.model.CacheInfoWithExpiry;
import com.soybeany.system.cache.server.repository.FileInfoRepository;
import com.soybeany.system.cache.server.repository.LocalFileInfo;
import com.soybeany.system.cache.server.util.SaveUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Soybeany
 * @date 2020/12/4
 */
public interface CacheInfoService {

    /**
     * 保障文件，并返回文件信息
     */
    LocalFileInfo ensureFileAndGetFileInfo(FileUid fileUid) throws IOException;

    /**
     * 获取缓存信息
     */
    FileInfo getCacheInfo(FileUid fileUid) throws IOException;

    File getDataFile(FileUid fileUid);

}

@Service
class CacheInfoServiceImpl implements CacheInfoService {

    /**
     * 默认的允许最大不活跃时间
     */
    private static final long DEFAULT_MAX_INACTIVE_SEC = 5 * 24 * 60 * 60;

    private static final Logger LOG = LoggerFactory.getLogger(CacheInfoServiceImpl.class);

    @Autowired
    private AppConfig appConfig;
    @Autowired
    private ConfigService configService;
    @Autowired
    private DownloadService downloadService;
    @Autowired
    private FileInfoRepository fileInfoRepository;

    private final Map<String, Lock> lockMap = new WeakHashMap<>();

    @Override
    public FileInfo getCacheInfo(FileUid fileUid) throws IOException {
        Lock lock = tryLock(fileUid);
        try {
            LocalFileInfo localFileInfo = ensureFileAndGetFileInfo(fileUid);
            // 修改访问记录
            localFileInfo.visitCount++;
            updateExpiryTime(localFileInfo);
            SaveUtils.syncSave(fileInfoRepository, localFileInfo);
            // 生成内容信息
            return new FileInfo(localFileInfo.contentDisposition,
                    Optional.ofNullable(localFileInfo.contentLength).orElse(-1L), localFileInfo.eTag);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public LocalFileInfo ensureFileAndGetFileInfo(FileUid fileUid) throws IOException {
        Lock lock = tryLock(fileUid);
        try {
            File localFile = getDataFile(fileUid);
            boolean fileExist = localFile.exists();
            // 检查文件信息是否存在
            String fUid = FileUid.toString(fileUid);
            LocalFileInfo localFileInfo = fileInfoRepository.findByFileUid(fUid);
            // 不存在则创建新信息
            boolean hasFileInfo = true, fileComplete = false;
            if (null == localFileInfo) {
                hasFileInfo = false;
                localFileInfo = new LocalFileInfo();
                localFileInfo.fileUid = fUid;
            }
            // 存在则进一步校验
            else if (fileExist) {
                fileComplete = CacheInfo.isFileComplete(localFileInfo.contentLength, localFile);
            }
            // 按需从数据源获取文件
            if (!fileExist || !hasFileInfo || !fileComplete) {
                LOG.info("“" + fileUid.fileToken + "”将重新获取文件，原因:" + "fileExist-" + fileExist + ",hasFileInfo-" + hasFileInfo + ",fileComplete-" + false);
                CacheInfoWithExpiry info = downloadService.downloadFile(fileUid, localFile);
                // 更新信息
                setupFileInfoWithContentInfo(localFileInfo, info);
            }
            // 设置为已下载
            localFileInfo.downloaded = true;
            updateExpiryTime(localFileInfo);
            return SaveUtils.syncSave(fileInfoRepository, localFileInfo);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public File getDataFile(FileUid fileUid) {
        return new File(configService.getCacheDir(fileUid.server), fileUid.fileToken);
    }

    private void updateExpiryTime(LocalFileInfo localFileInfo) {
        localFileInfo.expiryTime = CacheCoreTimeUtils.toDate(LocalDateTime.now().plusSeconds(localFileInfo.maxInactiveSec));
    }

    private void setupFileInfoWithContentInfo(LocalFileInfo localFileInfo, CacheInfoWithExpiry contentInfoWithExpiry) {
        localFileInfo.maxInactiveSec = getExpirySec(contentInfoWithExpiry.getExpirySec());
        localFileInfo.eTag = contentInfoWithExpiry.getEtag();
        localFileInfo.contentType = contentInfoWithExpiry.getContentType();
        localFileInfo.contentLength = contentInfoWithExpiry.getContentLength();
        localFileInfo.contentDisposition = contentInfoWithExpiry.getContentDisposition();
    }

    private long getExpirySec(Long expirySec) {
        return null != expirySec ? expirySec : DEFAULT_MAX_INACTIVE_SEC;
    }

    private Lock tryLock(FileUid fileUid) {
        Lock lock;
        synchronized (lockMap) {
            lock = lockMap.computeIfAbsent(FileUid.toString(fileUid), u -> new ReentrantLock());
        }
        try {
            if (!lock.tryLock(appConfig.contentInfoLockTimeoutSec, TimeUnit.SECONDS)) {
                throw new RuntimeException("锁获取等待超时");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("线程被中断");
        }
        return lock;
    }

}
