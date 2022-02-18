package com.soybeany.system.cache.demo.manager.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;

/**
 * 基础实体
 *
 * @author Soybeany
 * @date 2020/7/28
 */
@MappedSuperclass
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    @Column(nullable = false)
    private Integer version;

    public Long getId() {
        return id;
    }

    public Integer getVersion() {
        return version;
    }

    public boolean isNew() {
        return null == id;
    }

    /**
     * 带时间戳的基础实体
     */
    @MappedSuperclass
    @EntityListeners(AuditingEntityListener.class)
    public abstract static class WithTimeStamp extends BaseEntity {

        /**
         * 条目的创建时间
         */
        @CreatedDate
        @Column(nullable = false)
        private Date createTime;

        /**
         * 条目最后一次修改的时间
         */
        @LastModifiedDate
        @Column(nullable = false)
        private Date lastModifyTime;

        public Date getCreateTime() {
            return createTime;
        }

        public Date getLastModifyTime() {
            return lastModifyTime;
        }
    }

}
