package com.soybeany.system.cache.server.repository;

import com.soybeany.system.cache.server.model.FileInfoP;
import com.soybeany.system.cache.server.model.TaskInfoP;
import com.soybeany.system.cache.server.model.TempFileInfoP;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * @author Soybeany
 * @date 2022/2/16
 */
public interface DbDAO {

    void saveFileInfo(FileInfoP info);

    void saveTempFileInfo(TempFileInfoP info);

    void saveTaskInfo(TaskInfoP info);

    void deleteTempFileInfo(TempFileInfoP info);

    <T> void saveAll(String tableDesc, CrudRepository<T, Long> repository, List<? extends T> list);

    <T> void deleteAll(String tableDesc, CrudRepository<T, Long> repository, List<? extends T> list);

}
