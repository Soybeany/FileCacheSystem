package com.soybeany.system.cache.demo.manager.repository;


import org.springframework.lang.Nullable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

/**
 * 待主动下载的任务列表
 *
 * @author Soybeany
 * @date 2020/12/18
 */
@Entity
@Table(name = "task_info", indexes = {@Index(columnList = "lastModifyTime")})
public class TaskInfo extends BaseEntity.WithTimeStamp {

    /**
     * 文件标签，表示一个唯一的任务
     */
    @Column(nullable = false, unique = true)
    public String fileUid;

    /**
     * 可执行的开始时间(小时:0~23)
     */
    @Nullable
    @Column
    public int canExeFrom;

    /**
     * 可执行的结束时间(小时:0~23)
     */
    @Nullable
    @Column
    public int canExeTo;

}
