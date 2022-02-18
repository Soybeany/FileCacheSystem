package com.soybeany.system.cache.manager.model;

import com.soybeany.system.cache.core.model.Payload;
import com.soybeany.util.HexUtils;
import com.soybeany.util.SerializeUtils;
import lombok.Data;

import javax.crypto.SecretKey;
import java.util.UUID;

/**
 * @author Soybeany
 * @date 2020/12/25
 */
@Data
public class SecretKeyInfo {

    /**
     * 用于映射的key
     */
    private final String key;

    /**
     * 具体的密钥json
     */
    private final String secretKeyJson;

    /**
     * 创建时间戳
     */
    private final long createTimestamp;

    public static SecretKeyInfo getDefaultNew(long createTimestamp) throws Exception {
        String key = UUID.randomUUID().toString().replaceAll("-", "").substring(16);
        String secretKeyJson = fromSecretKey(Payload.generateNewKey());
        return new SecretKeyInfo(key, secretKeyJson, createTimestamp);
    }

    public static String fromSecretKey(SecretKey secretKey) throws Exception {
        return HexUtils.bytesToHex(SerializeUtils.serialize(secretKey));
    }

    public SecretKey getSecretKey() throws Exception {
        return SerializeUtils.deserialize(HexUtils.hexToByteArray(secretKeyJson));
    }

}
