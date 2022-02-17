package com.soybeany.system.cache.core.interfaces;

import com.soybeany.system.cache.core.model.SecretKeyInfo;

import java.util.List;

/**
 * @author Soybeany
 * @date 2022/1/26
 */
public interface ISecretKeyRepository {
    /**
     * 获取密钥列表
     */
    List<SecretKeyInfo> getSecretKeyList() throws Exception;

    /**
     * 生成指定数目的新密钥
     */
    void generateNewSecretKeys(int count) throws Exception;

    /**
     * 移除指定的密钥
     */
    void removeSecretKeys(List<SecretKeyInfo> keyInfoList);

    /**
     * 获取当前时间戳
     */
    long getCurrentTimestamp();
}
