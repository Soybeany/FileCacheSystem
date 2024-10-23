package com.soybeany.system.cache.core.util;

import com.google.gson.Gson;
import com.soybeany.exception.BdRtException;
import com.soybeany.system.cache.core.dto.FileUid;

import javax.crypto.SecretKey;
import java.util.function.Function;

public class TokenUtils {

    private static final Gson GSON = new Gson();
    private static final String SEPARATOR = "-";

    public static SecretKey generateNewKey() {
        return AesUtils.generateKey();
    }

    public static String toToken(Function<String, SecretKey> keyProvider, String key, FileUid fileUid) {
        String decryptPayloadStr = GSON.toJson(new DecryptedPayload(fileUid.fileId));
        String payload = AesUtils.encrypt(decryptPayloadStr, keyProvider.apply(key));
        return toToken(new TokenPart(fileUid.server, key, payload));
    }

    public static FileUid fromToken(Function<String, SecretKey> keyProvider, String token) {
        TokenPart tokenPart = fromToken(token);
        String payloadStr = AesUtils.decrypt(tokenPart.payload, keyProvider.apply(tokenPart.key));
        DecryptedPayload decryptedPayload = GSON.fromJson(payloadStr, DecryptedPayload.class);
        return new FileUid(tokenPart.server, decryptedPayload.fileId);
    }

    // ***********************内部方法****************************

    private static String toToken(TokenPart part) {
        return part.server + SEPARATOR + part.key + SEPARATOR + part.payload;
    }

    private static TokenPart fromToken(String fileToken) {
        String[] parts = fileToken.split(SEPARATOR);
        if (parts.length != 3) {
            throw new BdRtException("token格式不正确");
        }
        return new TokenPart(parts[0], parts[1], parts[2]);
    }

    // ***********************内部类****************************

    private static class TokenPart {
        final String server;
        final String key;
        final String payload;

        public TokenPart(String server, String key, String payload) {
            this.server = server;
            this.key = key;
            this.payload = payload;
        }
    }

    private static class DecryptedPayload {
        final String fileId;

        public DecryptedPayload(String fileId) {
            this.fileId = fileId;
        }
    }

}
