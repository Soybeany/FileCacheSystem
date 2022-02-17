package com.soybeany.system.cache.server.service;

import com.soybeany.system.cache.core.model.FileUid;
import com.soybeany.system.cache.core.service.BaseTimerService;
import com.soybeany.system.cache.core.util.CacheCoreTimeUtils;
import com.soybeany.system.cache.server.model.FileInfoP;
import com.soybeany.system.cache.server.model.TaskInfoP;
import com.soybeany.system.cache.server.repository.DbDAO;
import com.soybeany.system.cache.server.repository.FileInfoRepository;
import com.soybeany.system.cache.server.repository.TaskInfoRepository;
import com.soybeany.system.cache.server.repository.TempFileRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Soybeany
 * @date 2022/2/17
 */
@Slf4j
@Service
class CleanServiceImpl extends BaseTimerService {

    @Autowired
    private ConfigService configService;
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private DbDAO dbDAO;
    @Autowired
    private FileInfoRepository fileInfoRepository;
    @Autowired
    private TaskInfoRepository taskInfoRepository;
    @Autowired
    private TempFileRepository tempFileRepository;

    @Override
    protected void onSignal() {
        // 清理过期文件信息与文件
        try {
            cleanFileAndInfo();
        } catch (Exception e) {
            log.warn("过期文件信息与文件清理异常:" + e.getMessage());
        }
        // 清理未知文件
        try {
            cleanUnknownFile();
        } catch (Exception e) {
            log.warn("未知文件清理异常:" + e.getMessage());
        }
        // 清理过期任务
        try {
            cleanTask();
        } catch (Exception e) {
            log.warn("过期任务清理异常:" + e.getMessage());
        }
        // 清理过期临时文件信息
        try {
            cleanTempFileAndInfo();
        } catch (Exception e) {
            log.warn("过期临时文件信息与临时文件清理异常:" + e.getMessage());
        }
    }

    @Override
    protected int onSetupIntervalSec() {
        return configService.get().getCleanIntervalSec();
    }

    // ***********************内部方法****************************

    @PostConstruct
    public void onInit() {
        init();
    }

    @PreDestroy
    public void onDestroy() {
        destroy();
    }

    private void cleanFileAndInfo() {
        int count = 0;
        List<FileInfoP> exceedList = fileInfoRepository.selectAllExceedRecords(System.currentTimeMillis());
        for (FileInfoP info : exceedList) {
            FileUid fileUid = FileUid.fromString(info.fileUid);
            count = deleteFile(fileStorageService.loadFile(fileUid), count);
        }
        log.info("成功删除了" + count + "个缓存文件");
        dbDAO.deleteAll("FileInfo", fileInfoRepository, exceedList);
    }

    private void cleanUnknownFile() {
        int count = 0;
        for (String server : fileStorageService.getServers()) {
            File[] files = fileStorageService.getFiles(server);
            if (null == files) {
                continue;
            }
            for (File file : files) {
                String fileUidStr = FileUid.toFileUid(server, file.getName());
                if (!fileInfoRepository.existsFileInfoByFileUid(fileUidStr)) {
                    count = deleteFile(file, count);
                }
            }
        }
        log.info("成功删除了" + count + "个未知文件");
    }

    private void cleanTask() {
        List<TaskInfoP> exceedList = taskInfoRepository.selectAllExceedRecords(TaskInfoP.PRIORITY_FINISH, getExpiryTime(30));
        dbDAO.deleteAll("TaskInfo", taskInfoRepository, exceedList);
    }

    private void cleanTempFileAndInfo() {
        long expiryTime = getExpiryTime(7);
        // 删除过期的表记录
        dbDAO.deleteAll("TempFileInfo", tempFileRepository, tempFileRepository.selectAllExceedRecords(expiryTime));
        // 删除过期的文件
        File[] tempFiles = new File(fileStorageService.getTempFileDir()).listFiles();
        if (null == tempFiles) {
            return;
        }
        int count = 0;
        for (File tempFile : tempFiles) {
            if (tempFile.lastModified() < expiryTime) {
                count = deleteFile(tempFile, count);
            }
        }
        log.info("成功删除了" + count + "个临时文件");
    }

    private long getExpiryTime(int day) {
        return CacheCoreTimeUtils.toMillis(LocalDateTime.now().minusDays(day));
    }

    private int deleteFile(File file, int count) {
        boolean deleted = file.delete();
        return deleted ? count + 1 : count;
    }

}
