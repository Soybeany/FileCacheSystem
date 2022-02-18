package com.soybeany.system.cache.demo.manager.repository;

import com.soybeany.system.cache.demo.manager.model.TaskInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

/**
 * @author Soybeany
 * @date 2020/12/23
 */
public interface TaskInfoRepository extends JpaRepository<TaskInfo, Long> {

    TaskInfo findByFileUid(String fileUid);

    List<TaskInfo> findByLastModifyTimeAfterAndLastModifyTimeBeforeOrderByLastModifyTimeDesc(Date after, Date before);

}
