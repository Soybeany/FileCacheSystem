package com.soybeany.system.cache.server.service;

import com.soybeany.system.cache.core.model.FileUid;
import com.soybeany.system.cache.core.util.CacheCoreTimeUtils;
import com.soybeany.system.cache.server.exception.CacheServerException;
import com.soybeany.system.cache.server.exception.FileNotReadyException;
import com.soybeany.system.cache.server.model.CacheFileInfo;
import com.soybeany.system.cache.server.model.FileInfoP;
import com.soybeany.system.cache.server.repository.DbDAO;
import com.soybeany.util.file.BdFileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Soybeany
 * @date 2022/2/16
 */
@Service
public class FileManagerServiceImpl implements FileManagerService {

    private static final Map<String, Lock> LOCK_MAP = new WeakHashMap<>();

    @Autowired
    private ConfigService configService;
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private FileDownloadService fileDownloadService;
    @Autowired
    private DbDAO dbDAO;

    @Override
    public CacheFileInfo getFile(FileUid fileUid) throws CacheServerException {
        return getFile(fileUid, configService.get().getLockTimeoutSec());
    }

    @Override
    public CacheFileInfo getFile(FileUid fileUid, int lockTimeoutSec) throws CacheServerException {
        String fileUidStr = FileUid.toString(fileUid);
        try {
            return operateWithLock(lockTimeoutSec, fileUidStr, () -> onGetFile(fileUid, fileUidStr));
        } catch (InterruptedException e) {
            throw new FileNotReadyException();
        }
    }

    // ***********************内部方法****************************

    private CacheFileInfo onGetFile(FileUid fileUid, String fileUidStr) throws CacheServerException {
        // 获取缓存文件信息
        CacheFileInfo fileInfo = retrieveFile(fileUid, fileUidStr);
        // 保存文件信息
        saveFileInfo(fileUidStr, fileInfo);
        // 返回信息
        return fileInfo;
    }

    private void saveFileInfo(String fileUidStr, CacheFileInfo info) {
        FileInfoP entity = dbDAO.findFileInfoByFileUid(fileUidStr).orElseGet(() -> {
            FileInfoP infoP = new FileInfoP();
            infoP.fileUid = fileUidStr;
            return infoP;
        });
        entity.expiryTime = CacheCoreTimeUtils.toDate(LocalDateTime.now().plusSeconds(info.getAge()));
        entity.storageName = info.getFile().getName();
        entity.visitCount++;
        entity.eTag = info.eTag();
        entity.contentType = info.contentType();
        entity.contentDisposition = info.contentDisposition();
        entity.contentLength = info.contentLength();
        dbDAO.saveFileInfo(entity);
    }

    private CacheFileInfo retrieveFile(FileUid fileUid, String fileUidStr) throws CacheServerException {
        FileInfoP info = dbDAO.findFileInfoByFileUid(fileUidStr).orElse(null);
        // 表中没有记录，则按新文件下载
        if (null == info) {
            return fileDownloadService.downloadFile(fileUid, BdFileUtils.getUuid());
        }
        // 本地若有文件，直接返回
        File file = fileStorageService.loadFile(fileUid, info.storageName);
        if (file.exists()) {
            long contentLength = Optional.ofNullable(info.contentLength).orElse(-1L);
            int age = (int) (info.expiryTime.getTime() - System.currentTimeMillis()) / 1000;
            return new CacheFileInfo(info.contentDisposition, contentLength, info.eTag, age, file);
        }
        // 重新下载文件后返回
        return fileDownloadService.downloadFile(fileUid, info.storageName);
    }

    private CacheFileInfo operateWithLock(int lockTimeoutSec, String fileUid, ICallback callback) throws InterruptedException, CacheServerException {
        // 获取或创建锁对象
        Lock lock;
        synchronized (LOCK_MAP) {
            lock = LOCK_MAP.computeIfAbsent(fileUid, key -> new ReentrantLock());
        }
        // 尝试获取锁
        try {
            if (!lock.tryLock(lockTimeoutSec, TimeUnit.SECONDS)) {
                throw new InterruptedException();
            }
            return callback.onExecute();
        } finally {
            lock.unlock();
        }
    }

    // ***********************内部类****************************

    private interface ICallback {
        CacheFileInfo onExecute() throws CacheServerException;
    }

}
