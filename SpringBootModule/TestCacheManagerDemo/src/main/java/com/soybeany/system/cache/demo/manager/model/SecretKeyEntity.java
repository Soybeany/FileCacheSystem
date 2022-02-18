package com.soybeany.system.cache.demo.manager.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author Soybeany
 * @date 2020/12/25
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "secret_key_info")
public class SecretKeyEntity extends BaseEntity {
    /**
     * 用于映射的key
     */
    @Column(nullable = false, unique = true)
    private String key;

    /**
     * 具体的密钥json
     */
    @Column(nullable = false)
    private String secretKeyJson;

    /**
     * 创建时间戳
     */
    @Column(nullable = false)
    private long createTimestamp;

}
