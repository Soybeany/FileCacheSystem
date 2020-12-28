package com.soybeany.system.cache.server.service;

import com.soybeany.system.cache.core.model.FileUid;
import com.soybeany.system.cache.server.config.AppConfig;
import com.soybeany.system.cache.server.config.ServerInfo;
import com.soybeany.system.cache.server.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Soybeany
 * @date 2020/12/18
 */
public interface CleanService {

    /**
     * 清除文件
     */
    int cleanFiles();

    /**
     * 清除表记录
     */
    int cleanRecords();

}

@Service
class CleanServiceImpl implements CleanService {

    private static final Logger LOG = LoggerFactory.getLogger(CleanServiceImpl.class);

    @Autowired
    private AppConfig appConfig;
    @Autowired
    private ConfigService configService;
    @Autowired
    private FileInfoRepository fileInfoRepository;
    @Autowired
    private TempFileRepository tempFileRepository;
    @Autowired
    private TaskInfoRepository taskInfoRepository;

    @Override
    public int cleanFiles() {
        int count = 0;
        count += cleanInvalidFiles();
        count += cleanUnknownFiles();
        count += cleanTempFiles();
        return count;
    }

    @Override
    public int cleanRecords() {
        int count = 0;
        count += cleanInvalidFileInfo();
        count += cleanInvalidTempFileInfo();
        count += cleanInvalidTaskInfo();
        return count;
    }

    private int cleanInvalidFiles() {
        int cleanCount = 0;
        for (FileInfo info : fileInfoRepository.selectAllExceedRecords(System.currentTimeMillis(), true)) {
            FileUid fileUid = FileUid.fromString(info.fileUid);
            cleanCount = deleteFile(fileUid.server, new File(configService.getCacheDir(fileUid.server), fileUid.fileToken), cleanCount);
            info.downloaded = false;
            fileInfoRepository.save(info);
        }
        return cleanCount;
    }

    private int cleanUnknownFiles() {
        int cleanCount = 0;
        File parent = new File(appConfig.fileCacheDir);
        for (ServerInfo info : appConfig.appServers) {
            File dir = new File(parent, info.name);
            String[] fileNames = dir.list();
            if (null == fileNames) {
                continue;
            }
            for (String fileName : fileNames) {
                if (!fileInfoRepository.existsFileInfoByFileUidAndDownloadedIsTrue(FileUid.toFileUid(info.name, fileName))) {
                    cleanCount = deleteFile(info.name, new File(configService.getCacheDir(info.name), fileName), cleanCount);
                }
            }
        }
        return cleanCount;
    }

    private int cleanTempFiles() {
        File tempDir = configService.getTempDir();
        String[] fileNames = tempDir.list();
        if (null == fileNames) {
            return 0;
        }
        int cleanCount = 0;
        long curTimestamp = System.currentTimeMillis();
        long expiry = appConfig.tempFileExpirySec * 1000;
        for (String fileName : fileNames) {
            File tempFile = new File(tempDir, fileName);
            if (curTimestamp - tempFile.lastModified() > expiry) {
                cleanCount = deleteFile("临时目录", tempFile, cleanCount);
            }
        }
        return cleanCount;
    }

    private int cleanInvalidFileInfo() {
        List<FileInfo> list = fileInfoRepository.selectAllExceedRecords(getExpiryTime(), false);
        List<String> uidList = new LinkedList<>();
        for (FileInfo info : list) {
            uidList.add(info.fileUid);
        }
        return deleteRecords("FileInfo", fileInfoRepository, list, uidList);
    }

    private int cleanInvalidTempFileInfo() {
        List<TempFileInfo> list = tempFileRepository.selectAllExceedRecords(getExpiryTime());
        List<String> uidList = new LinkedList<>();
        for (TempFileInfo info : list) {
            uidList.add(info.fileUid);
        }
        return deleteRecords("TempFileInfo", tempFileRepository, list, uidList);
    }

    private int cleanInvalidTaskInfo() {
        List<TaskInfo> list = taskInfoRepository.selectAllExceedRecords(TaskInfo.PRIORITY_FINISH, getExpiryTime());
        List<String> uidList = new LinkedList<>();
        for (TaskInfo info : list) {
            uidList.add(info.fileUid);
        }
        return deleteRecords("TaskInfo", taskInfoRepository, list, uidList);
    }

    private long getExpiryTime() {
        return System.currentTimeMillis() - appConfig.recordExpirySec * 1000;
    }

    private <T> int deleteRecords(String tableDesc, CrudRepository<T, Long> repository, List<? extends T> list, List<String> uidList) {
        if (list.isEmpty()) {
            return 0;
        }
        try {
            repository.deleteAll(list);
            LOG.info(tableDesc + "记录删除成功:" + uidList);
            return list.size();
        } catch (Exception e) {
            LOG.warn(tableDesc + "记录删除异常:" + uidList);
            LOG.warn("异常原因:" + e.getMessage());
            throw e;
        }
    }

    private int deleteFile(String dirDesc, File file, int count) {
        boolean deleted = file.delete();
        LOG.info(dirDesc + "的文件“" + file.getName() + "”删除" + (deleted ? "成功" : "失败"));
        return deleted ? count + 1 : count;
    }
}
