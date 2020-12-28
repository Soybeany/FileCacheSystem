package com.soybeany.system.cache.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * @author Soybeany
 * @date 2020/12/18
 */
public interface TempFileRepository extends JpaRepository<TempFileInfo, Long> {

    TempFileInfo findByFileUid(String fileUid);

    /**
     * 查询全部失效的记录
     */
    @Query(value = "select * " +
            "from temp_file_info " +
            "where last_modify_time < :expiryTime",
            nativeQuery = true)
    List<TempFileInfo> selectAllExceedRecords(long expiryTime);

}
