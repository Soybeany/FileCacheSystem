package com.soybeany.system.cache.server.repository;

import com.soybeany.system.cache.server.model.TaskInfoP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * @author Soybeany
 * @date 2020/12/18
 */
public interface TaskInfoRepository extends JpaRepository<TaskInfoP, Long> {

    TaskInfoP findByFileUid(String fileUid);

    @Query(value = "from TaskInfoP " +
            "where priority > " + TaskInfoP.PRIORITY_FINISH + " " +
            "and canExeFrom <= :curHour " +
            "and canExeTo >= :curHour " +
            "order by priority desc")
    List<TaskInfoP> findTasksToExecute(int curHour);

    /**
     * 查询全部失效的记录
     */
    @Query(value = "select * " +
            "from task_info " +
            "where priority <= :priority " +
            "and last_modify_time < :minValidTime",
            nativeQuery = true)
    List<TaskInfoP> selectAllExceedRecords(int priority, long minValidTime);

}
