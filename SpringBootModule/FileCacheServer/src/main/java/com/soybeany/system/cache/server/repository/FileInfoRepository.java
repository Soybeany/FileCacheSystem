package com.soybeany.system.cache.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * @author Soybeany
 * @date 2020/12/4
 */
public interface FileInfoRepository extends JpaRepository<FileInfo, Long> {

    /**
     * 查找指定fileToken的记录
     */
    FileInfo findByFileUid(String fileUid);

    /**
     * 判断指定fileToken的记录是否存在
     */
    boolean existsFileInfoByFileUidAndDownloadedIsTrue(String fileUid);

    /**
     * 查询全部失效的记录
     */
    @Query(value = "select * " +
            "from file_info " +
            "where expiry_time < :expiryTime " +
            "and downloaded = :downloaded",
            nativeQuery = true)
    List<FileInfo> selectAllExceedRecords(long expiryTime, boolean downloaded);

}
