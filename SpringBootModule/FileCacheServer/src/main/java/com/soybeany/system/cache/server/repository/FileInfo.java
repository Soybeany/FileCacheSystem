package com.soybeany.system.cache.server.repository;

import org.springframework.lang.Nullable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import java.util.Date;

/**
 * 本地文件的信息
 *
 * @author Soybeany
 * @date 2020/12/4
 */
@Entity
@Table(name = "file_info",
        indexes = {@Index(columnList = "expiryTime")})
public class FileInfo extends BaseEntity.WithTimeStamp {

    // ********************框架属性********************

    /**
     * 文件uid
     */
    @Column(nullable = false, unique = true)
    public String fileUid;

    /**
     * 记录超时时间，由最后修改时间加上最大允许不活跃秒数得到
     */
    @Column(nullable = false)
    public Date expiryTime;

    /**
     * 最大允许不活跃的秒数
     * <br>不活跃的秒数：最后一次访问距离现在的秒数
     */
    @Column(nullable = false)
    public long maxInactiveSec;

    /**
     * 该文件被访问的次数
     */
    @Column(nullable = false)
    public int visitCount;

    /**
     * 文件是否已下载
     */
    @Column(nullable = false)
    public boolean downloaded;

    // ********************文件属性********************

    /**
     * 文件标识，业务服务器返回
     */
    @Nullable
    @Column
    public String eTag;

    /**
     * 内容类型
     */
    @Nullable
    @Column
    public String contentType;

    /**
     * 内容长度
     */
    @Nullable
    @Column
    public Long contentLength;

    /**
     * 内容情况
     */
    @Nullable
    @Column
    public String contentDisposition;

}
