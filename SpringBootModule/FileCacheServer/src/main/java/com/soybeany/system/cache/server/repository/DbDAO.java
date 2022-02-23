package com.soybeany.system.cache.server.repository;

import com.soybeany.system.cache.server.model.FileInfoP;
import com.soybeany.system.cache.server.model.TaskInfoP;
import com.soybeany.system.cache.server.model.TempFileInfoP;

import java.util.List;
import java.util.Optional;

/**
 * @author Soybeany
 * @date 2022/2/16
 */
public interface DbDAO {

    // ***********************缓存文件****************************

    Optional<FileInfoP> findFileInfoByFileUid(String fileUid);

    boolean existsFileInfoByFileUidOrStorageName(String fileUid, String storageName);

    List<FileInfoP> selectFileInfoAllExceedRecords(long expiryTime);

    void saveFileInfo(FileInfoP info);

    void deleteAllFileInfo(List<FileInfoP> list);

    // ***********************临时文件****************************

    Optional<TempFileInfoP> findTempFileInfoByFileUid(String fileUid);

    List<TempFileInfoP> selectTempFileInfoAllExceedRecords(long expiryTime);

    void saveTempFileInfo(TempFileInfoP info);

    void deleteTempFileInfo(TempFileInfoP info);

    void deleteAllTempFileInfo(List<TempFileInfoP> list);

    // ***********************任务****************************

    TaskInfoP findTaskInfoByFileUid(String fileUid);

    List<TaskInfoP> findTaskInfoTasksToExecute(int curHour);

    List<TaskInfoP> selectTaskInfoAllExceedRecords(int priority, long minValidTime);

    Long selectTaskInfoNewestStamp();

    void saveTaskInfo(TaskInfoP info);

    void saveAllTaskInfo(List<TaskInfoP> list);

    void deleteAllTaskInfo(List<TaskInfoP> list);

}
