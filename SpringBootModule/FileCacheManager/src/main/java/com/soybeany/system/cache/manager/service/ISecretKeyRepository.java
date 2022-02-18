package com.soybeany.system.cache.manager.service;

import com.soybeany.system.cache.manager.model.SecretKeyInfo;

import java.util.List;

/**
 * @author Soybeany
 * @date 2022/1/26
 */
public interface ISecretKeyRepository {
    /**
     * 获取密钥列表
     */
    List<SecretKeyInfo> getSecretKeys();

    /**
     * 新增指定的密钥
     */
    void addSecretKey(SecretKeyInfo info);

    /**
     * 移除指定的密钥
     */
    void deleteSecretKey(String key);

    /**
     * 获取当前时间戳
     */
    long getCurrentTimestamp();
}
