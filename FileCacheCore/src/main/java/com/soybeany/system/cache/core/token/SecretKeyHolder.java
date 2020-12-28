package com.soybeany.system.cache.core.token;

import com.soybeany.util.HexUtils;
import com.soybeany.util.SerializeUtils;

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
    public String newestKey;

    public SecretKeyHolder() {
    }

    public SecretKeyHolder(SecretKeyHolder holder) {
        map.putAll(holder.map);
        newestKey = holder.newestKey;
    }

    public SecretKey getSecretKey(String key) {
        if (!map.containsKey(key)) {
            throw new RuntimeException("没有找到指定的secretKey");
        }
        return map.get(key);
    }

    public static class WithExpiry extends SecretKeyHolder {

        /**
         * 失效时间(millis)
         */
        public final int expiryMillis;

        public static WithExpiry deserialize(String content) throws Exception {
            return SerializeUtils.deserialize(HexUtils.hexToByteArray(content));
        }

        public static String serialize(WithExpiry obj) throws Exception {
            return HexUtils.bytesToHex(SerializeUtils.serialize(obj));
        }

        public WithExpiry(SecretKeyHolder holder, long expiryMillis) {
            super(holder);
            this.expiryMillis = (int) expiryMillis;
        }
    }
}
