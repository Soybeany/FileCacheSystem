package com.soybeany.system.cache.core.token;

import javax.crypto.SecretKey;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Soybeany
 * @date 2020/12/11
 */
public abstract class SecretKeyHolder implements Serializable {

    /**
     * 当前生效的列表
     */
    public final Map<String, SecretKey> map = new HashMap<>();

    /**
     * 最新密钥的key
     */
    public final String newestKey;

    public SecretKeyHolder(String newestKey) {
        this.newestKey = newestKey;
    }

    public SecretKeyHolder(SecretKeyHolder holder) {
        this(holder.newestKey);
        map.putAll(holder.map);
    }

    public SecretKey getSecretKey(String key) {
        if (!map.containsKey(key)) {
            throw new RuntimeException("没有找到指定的secretKey");
        }
        return map.get(key);
    }

    public static class WithTtl extends SecretKeyHolder {

        /**
         * 存活时间(millis)
         */
        public final int pTtl;

        public WithTtl(SecretKeyHolder holder, int pTtl) {
            super(holder);
            this.pTtl = pTtl;
        }
    }
}
