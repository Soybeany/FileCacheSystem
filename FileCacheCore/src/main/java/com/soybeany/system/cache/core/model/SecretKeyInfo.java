package com.soybeany.system.cache.core.model;

import com.soybeany.system.cache.core.token.Payload;
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
    private String key;

    /**
     * 具体的密钥json
     */
    private String secretKeyJson;

    /**
     * 创建时间戳
     */
    private long createTimestamp;

    public static SecretKeyInfo getDefaultNew(long createTimestamp) throws Exception {
        SecretKeyInfo keyInfo = new SecretKeyInfo();
        keyInfo.key = UUID.randomUUID().toString().replaceAll("-", "").substring(16);
        keyInfo.secretKeyJson = fromSecretKey(Payload.generateNewKey());
        keyInfo.createTimestamp = createTimestamp;
        return keyInfo;
    }

    public static String fromSecretKey(SecretKey secretKey) throws Exception {
        return HexUtils.bytesToHex(SerializeUtils.serialize(secretKey));
    }

    public SecretKey toSecretKey() throws Exception {
        return SerializeUtils.deserialize(HexUtils.hexToByteArray(secretKeyJson));
    }

}
