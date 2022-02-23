package com.soybeany.system.cache.server.model;


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
@Table(name = "task_info",
        indexes = {
                @Index(name = "timeIndex", columnList = "canExeFrom"),
                @Index(name = "timeIndex", columnList = "canExeTo")
        }
)
public class TaskInfoP extends BaseEntity.WithTimeStamp {

    public static final int PRIORITY_FINISH = 0;

    /**
     * 文件uid
     */
    @Column(nullable = false, unique = true)
    public String fileUid;

    /**
     * 优先级，越大的值，越先被执行
     * <br>下载失败，自动降1级
     * <br>下载成功，降为{@link #PRIORITY_FINISH}
     */
    @Column
    public int priority;

    /**
     * 任务对应的stamp
     */
    @Column
    public long stamp;

    /**
     * 可执行的开始时间(小时:0~23)
     */
    @Column
    public int canExeFrom;

    /**
     * 可执行的结束时间(小时:0~23)
     */
    @Column
    public int canExeTo;

}
