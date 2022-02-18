package com.soybeany.system.cache.core.model;

import lombok.RequiredArgsConstructor;

import javax.crypto.SecretKey;
import java.io.Serializable;
import java.util.Map;

/**
 * @author Soybeany
 * @date 2022/2/18
 */
@RequiredArgsConstructor
public class SecretKeyHolder implements Serializable {

    /**
     * 当前生效的列表
     */
    public final Map<String, SecretKey> map;

    /**
     * 最新密钥的key
     */
    public final String newestKey;

    public SecretKey getSecretKey(String key) {
        if (!map.containsKey(key)) {
            throw new RuntimeException("没有找到指定的secretKey");
        }
        return map.get(key);
    }

}
