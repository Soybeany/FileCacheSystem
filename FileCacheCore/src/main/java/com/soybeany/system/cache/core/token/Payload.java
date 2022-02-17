package com.soybeany.system.cache.core.token;

import com.google.gson.Gson;

import javax.crypto.SecretKey;

/**
 * @author Soybeany
 * @date 2020/12/1
 */
public class Payload {

    private static final Gson GSON = new Gson();

    /**
     * 用于正式请求App服务器的fileId
     */
    public final String fileToken;

    public static SecretKey generateNewKey() throws Exception {
        return AesUtils.generateKey();
    }

    public static Payload fromString(String encryptedJson, SecretKey secretKey) throws Exception {
        String json = AesUtils.decrypt(encryptedJson, secretKey);
        return GSON.fromJson(json, Payload.class);
    }

    public static String fromPayload(Payload payload, SecretKey secretKey) throws Exception {
        String json = GSON.toJson(payload);
        return AesUtils.encrypt(json, secretKey);
    }

    public Payload(String fileToken) {
        this.fileToken = fileToken;
    }
}
