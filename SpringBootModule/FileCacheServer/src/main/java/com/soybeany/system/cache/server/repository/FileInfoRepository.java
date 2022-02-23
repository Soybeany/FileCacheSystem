package com.soybeany.system.cache.server.repository;

import com.soybeany.system.cache.server.model.FileInfoP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/**
 * @author Soybeany
 * @date 2020/12/4
 */
interface FileInfoRepository extends JpaRepository<FileInfoP, Long> {

    /**
     * 查找指定fileToken的记录
     */
    Optional<FileInfoP> findByFileUid(String fileUid);

    /**
     * 判断指定fileToken的记录是否存在
     */
    boolean existsByFileUidOrStorageName(String fileUid, String storageName);

    /**
     * 查询全部失效的记录
     */
    @Query(value = "select * " +
            "from file_info " +
            "where expiry_time < :expiryTime",
            nativeQuery = true)
    List<FileInfoP> selectAllExceedRecords(long expiryTime);

}
