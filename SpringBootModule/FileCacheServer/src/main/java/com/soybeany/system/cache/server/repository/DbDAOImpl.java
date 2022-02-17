package com.soybeany.system.cache.server.repository;

import com.soybeany.system.cache.server.model.FileInfoP;
import com.soybeany.system.cache.server.model.TaskInfoP;
import com.soybeany.system.cache.server.model.TempFileInfoP;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Soybeany
 * @date 2022/2/16
 */
@Slf4j
@Transactional
@Repository
public class DbDAOImpl implements DbDAO {

    @Autowired
    private FileInfoRepository fileInfoRepository;
    @Autowired
    private TempFileRepository tempFileRepository;
    @Autowired
    private TaskInfoRepository taskInfoRepository;

    @Override
    public synchronized void saveFileInfo(FileInfoP info) {
        fileInfoRepository.save(info);
    }

    @Override
    public synchronized void saveTempFileInfo(TempFileInfoP info) {
        tempFileRepository.save(info);
    }

    @Override
    public synchronized void saveTaskInfo(TaskInfoP info) {
        taskInfoRepository.save(info);
    }

    @Override
    public synchronized void deleteTempFileInfo(TempFileInfoP info) {
        tempFileRepository.delete(info);
    }

    @Override
    public synchronized <T> void saveAll(String tableDesc, CrudRepository<T, Long> repository, List<? extends T> list) {
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

    @Override
    public synchronized <T> void deleteAll(String tableDesc, CrudRepository<T, Long> repository, List<? extends T> list) {
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
