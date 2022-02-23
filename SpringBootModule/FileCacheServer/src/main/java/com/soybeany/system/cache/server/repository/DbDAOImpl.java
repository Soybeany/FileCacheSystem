package com.soybeany.system.cache.server.repository;

import com.soybeany.system.cache.server.model.FileInfoP;
import com.soybeany.system.cache.server.model.TaskInfoP;
import com.soybeany.system.cache.server.model.TempFileInfoP;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author Soybeany
 * @date 2022/2/16
 */
@Slf4j
@Repository
public class DbDAOImpl implements DbDAO {

    @Autowired
    private FileInfoRepository fileInfoRepository;
    @Autowired
    private TempFileRepository tempFileRepository;
    @Autowired
    private TaskInfoRepository taskInfoRepository;

    @Override
    public synchronized Optional<FileInfoP> findFileInfoByFileUid(String fileUid) {
        return fileInfoRepository.findByFileUid(fileUid);
    }

    @Override
    public synchronized boolean existsFileInfoByFileUidOrStorageName(String fileUid, String storageName) {
        return fileInfoRepository.existsByFileUidOrStorageName(fileUid, storageName);
    }

    @Override
    public synchronized List<FileInfoP> selectFileInfoAllExceedRecords(long expiryTime) {
        return fileInfoRepository.selectAllExceedRecords(expiryTime);
    }

    @Override
    public synchronized void saveFileInfo(FileInfoP info) {
        fileInfoRepository.save(info);
    }

    @Override
    public void deleteAllFileInfo(List<FileInfoP> list) {
        deleteAll("FileInfo", fileInfoRepository, list);
    }

    @Override
    public synchronized Optional<TempFileInfoP> findTempFileInfoByFileUid(String fileUid) {
        return tempFileRepository.findByFileUid(fileUid);
    }

    @Override
    public synchronized List<TempFileInfoP> selectTempFileInfoAllExceedRecords(long expiryTime) {
        return tempFileRepository.selectAllExceedRecords(expiryTime);
    }

    @Override
    public synchronized void saveTempFileInfo(TempFileInfoP info) {
        tempFileRepository.save(info);
    }

    @Override
    public synchronized void deleteTempFileInfo(TempFileInfoP info) {
        tempFileRepository.delete(info);
    }

    @Override
    public void deleteAllTempFileInfo(List<TempFileInfoP> list) {
        deleteAll("TempFileInfo", tempFileRepository, list);
    }

    @Override
    public synchronized TaskInfoP findTaskInfoByFileUid(String fileUid) {
        return taskInfoRepository.findByFileUid(fileUid);
    }

    @Override
    public synchronized List<TaskInfoP> findTaskInfoTasksToExecute(int curHour) {
        return taskInfoRepository.findTasksToExecute(curHour);
    }

    @Override
    public synchronized List<TaskInfoP> selectTaskInfoAllExceedRecords(int priority, long minValidTime) {
        return taskInfoRepository.selectAllExceedRecords(priority, minValidTime);
    }

    @Override
    public synchronized Long selectTaskInfoNewestStamp() {
        return taskInfoRepository.selectNewestStamp();
    }

    @Override
    public synchronized void saveTaskInfo(TaskInfoP info) {
        taskInfoRepository.save(info);
    }

    @Override
    public void saveAllTaskInfo(List<TaskInfoP> list) {
        saveAll("TaskInfo", taskInfoRepository, list);
    }

    @Override
    public void deleteAllTaskInfo(List<TaskInfoP> list) {
        deleteAll("TaskInfo", taskInfoRepository, list);
    }

    private synchronized <T> void saveAll(String tableDesc, CrudRepository<T, Long> repository, List<? extends T> list) {
        if (list.isEmpty()) {
            return;
        }
        try {
            repository.saveAll(list);
            log.info(tableDesc + "成功保存了" + list.size() + "条记录");
        } catch (Exception e) {
            log.warn(tableDesc + "保存记录异常，" + e.getMessage());
            throw e;
        }
    }

    private synchronized <T> void deleteAll(String tableDesc, CrudRepository<T, Long> repository, List<? extends T> list) {
        if (list.isEmpty()) {
            return;
        }
        try {
            repository.deleteAll(list);
            log.info(tableDesc + "成功删除了" + list.size() + "条记录");
        } catch (Exception e) {
            log.warn(tableDesc + "删除记录异常，" + e.getMessage());
            throw e;
        }
    }

}
