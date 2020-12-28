package com.soybeany.system.cache.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * @author Soybeany
 * @date 2020/12/18
 */
public interface TaskInfoRepository extends JpaRepository<TaskInfo, Long> {

    TaskInfo findByFileUid(String fileUid);

    List<TaskInfo> findByPriorityGreaterThanOrderByPriorityDesc(int priority);

    /**
     * 查询全部失效的记录
     */
    @Query(value = "select * " +
            "from task_info " +
            "where priority <= :priority " +
            "and last_modify_time < :minValidTime",
            nativeQuery = true)
    List<TaskInfo> selectAllExceedRecords(int priority, long minValidTime);

}
