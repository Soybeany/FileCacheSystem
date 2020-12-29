package com.soybeany.system.cache.demo.manager.repository;

import org.springframework.lang.Nullable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

/**
 * 缓存服务器的信息
 *
 * @author Soybeany
 * @date 2020/12/22
 */
@Entity
@Table(name = "cache_server_info")
public class CacheServerInfo extends BaseEntity.WithTimeStamp {

    /**
     * 服务器主机地址
     */
    @Column(nullable = false)
    public String host;

    /**
     * 访问需要的凭证
     */
    @Column(nullable = false)
    public String authorization;

    /**
     * 该服务器的描述，如所属分子公司
     */
    @Column(nullable = false)
    public String desc;

    /**
     * 已同步的时间戳
     */
    @Nullable
    @Column
    public Date syncedTimestamp;

    /**
     * 最后一次同步的时间，也用于心跳检测
     */
    @Nullable
    @Column
    public Date lastSyncTime;

}
