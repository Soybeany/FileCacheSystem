package com.soybeany.system.cache.server.repository;

import com.soybeany.system.cache.server.model.TempFileInfoP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/**
 * @author Soybeany
 * @date 2020/12/18
 */
public interface TempFileRepository extends JpaRepository<TempFileInfoP, Long> {

    Optional<TempFileInfoP> findByFileUid(String fileUid);

    /**
     * 查询全部失效的记录
     */
    @Query(value = "select * " +
            "from temp_file_info " +
            "where last_modify_time < :expiryTime",
            nativeQuery = true)
    List<TempFileInfoP> selectAllExceedRecords(long expiryTime);

}
