package com.soybeany.system.cache.core.security.model;

import com.soybeany.system.cache.core.util.TokenUtils;
import com.soybeany.util.HexUtils;
import com.soybeany.util.SerializeUtils;

import javax.crypto.SecretKey;
import java.util.UUID;

/**
 * @author Soybeany
 * @date 2020/12/25
 */
public class SecretKeyInfo {
    /**
     * 用于映射的key
     */
    public String key;
    /**
     * 具体的密钥json
     */
    public String secretKeyJson;
    /**
     * 创建时间戳
     */
    public long createTimestamp;

    public static SecretKeyInfo getDefaultNew(long createTimestamp) {
        SecretKeyInfo keyInfo = new SecretKeyInfo();
        keyInfo.key = UUID.randomUUID().toString().replaceAll("-", "").substring(16);
        keyInfo.secretKeyJson = fromSecretKey(TokenUtils.generateNewKey());
        keyInfo.createTimestamp = createTimestamp;
        return keyInfo;
    }

    public static String fromSecretKey(SecretKey secretKey) {
        try {
            return HexUtils.bytesToHex(SerializeUtils.serialize(secretKey));
        } catch (Exception e) {
            throw new FcException("secretKey序列化异常:" + e.getMessage());
        }
    }

    public SecretKey toSecretKey() {
        try {
            return SerializeUtils.deserialize(HexUtils.hexToByteArray(secretKeyJson));
        } catch (Exception e) {
            throw new FcException("secretKey反序列化异常:" + e.getMessage());
        }
    }

    @SuppressWarnings("unused")
    public String getKey() {
        return key;
    }

    @SuppressWarnings("unused")
    public void setKey(String key) {
        this.key = key;
    }

    @SuppressWarnings("unused")
    public String getSecretKeyJson() {
        return secretKeyJson;
    }

    @SuppressWarnings("unused")
    public void setSecretKeyJson(String secretKeyJson) {
        this.secretKeyJson = secretKeyJson;
    }

    @SuppressWarnings("unused")
    public long getCreateTimestamp() {
        return createTimestamp;
    }

    @SuppressWarnings("unused")
    public void setCreateTimestamp(long createTimestamp) {
        this.createTimestamp = createTimestamp;
    }
}
