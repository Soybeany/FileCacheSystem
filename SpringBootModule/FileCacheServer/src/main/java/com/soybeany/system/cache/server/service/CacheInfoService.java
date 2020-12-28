package com.soybeany.system.cache.server.service;

import com.soybeany.system.cache.core.model.FileUid;
import com.soybeany.system.cache.core.util.CacheCoreTimeUtils;
import com.soybeany.system.cache.server.config.AppConfig;
import com.soybeany.system.cache.server.model.CacheInfo;
import com.soybeany.system.cache.server.model.CacheInfoWithExpiry;
import com.soybeany.system.cache.server.model.CacheInfoWithFile;
import com.soybeany.system.cache.server.repository.FileInfo;
import com.soybeany.system.cache.server.repository.FileInfoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
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
    FileInfo ensureFileAndGetFileInfo(FileUid fileUid) throws IOException;

    /**
     * 获取缓存信息
     */
    CacheInfoWithFile getCacheInfo(FileUid fileUid) throws IOException;

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
    public CacheInfoWithFile getCacheInfo(FileUid fileUid) throws IOException {
        Lock lock = tryLock(fileUid);
        try {
            FileInfo fileInfo = ensureFileAndGetFileInfo(fileUid);
            // 修改访问记录
            fileInfo.visitCount++;
            updateExpiryTime(fileInfo);
            fileInfoRepository.save(fileInfo);
            // 生成内容信息
            return toContentInfo(getDataFile(fileUid), fileInfo);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public FileInfo ensureFileAndGetFileInfo(FileUid fileUid) throws IOException {
        Lock lock = tryLock(fileUid);
        try {
            File localFile = getDataFile(fileUid);
            boolean fileExist = localFile.exists();
            // 检查文件信息是否存在
            String fUid = FileUid.toString(fileUid);
            FileInfo fileInfo = fileInfoRepository.findByFileUid(fUid);
            // 不存在则创建新信息
            boolean hasFileInfo = true, fileComplete = false;
            if (null == fileInfo) {
                hasFileInfo = false;
                fileInfo = new FileInfo();
                fileInfo.fileUid = fUid;
            }
            // 存在则进一步校验
            else if (fileExist) {
                fileComplete = CacheInfo.isFileComplete(fileInfo.contentLength, localFile);
            }
            // 按需从数据源获取文件
            if (!fileExist || !hasFileInfo || !fileComplete) {
                LOG.info("“" + fileUid.fileToken + "”将重新获取文件，原因:" + "fileExist-" + fileExist + ",hasFileInfo-" + hasFileInfo + ",fileComplete-" + false);
                CacheInfoWithExpiry info = downloadService.downloadFile(fileUid, localFile);
                // 更新信息
                setupFileInfoWithContentInfo(fileInfo, info);
            }
            // 设置为已下载
            fileInfo.downloaded = true;
            updateExpiryTime(fileInfo);
            return fileInfoRepository.save(fileInfo);
        } finally {
            lock.unlock();
        }
    }

    private void updateExpiryTime(FileInfo fileInfo) {
        fileInfo.expiryTime = CacheCoreTimeUtils.toDate(LocalDateTime.now().plusSeconds(fileInfo.maxInactiveSec));
    }

    private CacheInfoWithFile toContentInfo(File file, FileInfo info) {
        CacheInfoWithFile contentInfo = new CacheInfoWithFile(file, info.eTag);
        contentInfo.setContentType(info.contentType);
        contentInfo.setContentLength(info.contentLength);
        contentInfo.setContentDisposition(info.contentDisposition);
        return contentInfo;
    }

    private void setupFileInfoWithContentInfo(FileInfo fileInfo, CacheInfoWithExpiry contentInfoWithExpiry) {
        fileInfo.maxInactiveSec = getExpirySec(contentInfoWithExpiry.getExpirySec());
        fileInfo.eTag = contentInfoWithExpiry.getEtag();
        fileInfo.contentType = contentInfoWithExpiry.getContentType();
        fileInfo.contentLength = contentInfoWithExpiry.getContentLength();
        fileInfo.contentDisposition = contentInfoWithExpiry.getContentDisposition();
    }

    private long getExpirySec(Long expirySec) {
        return null != expirySec ? expirySec : DEFAULT_MAX_INACTIVE_SEC;
    }

    private File getDataFile(FileUid fileUid) {
        return new File(configService.getCacheDir(fileUid.server), fileUid.fileToken);
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
