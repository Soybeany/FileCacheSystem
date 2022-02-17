package com.soybeany.system.cache.server.model;

import org.springframework.lang.Nullable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 临时文件的信息
 *
 * @author Soybeany
 * @date 2020/12/18
 */
@Entity
@Table(name = "temp_file_info")
public class TempFileInfoP extends BaseEntity.WithTimeStamp {

    /**
     * 文件uid
     */
    @Column(nullable = false, unique = true)
    public String fileUid;

    /**
     * 文件标识，业务服务器返回
     */
    @Nullable
    @Column
    public String eTag;

    /**
     * 临时文件名称
     */
    @Column(nullable = false)
    public String tempFileName;

    /**
     * 已下载的长度
     */
    @Column(nullable = false)
    public long downloadedLength;

    /**
     * 文件总长
     */
    @Nullable
    @Column
    public Long totalLength;

}
