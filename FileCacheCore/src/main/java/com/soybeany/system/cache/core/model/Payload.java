package com.soybeany.system.cache.core.model;

import com.google.gson.Gson;
import com.soybeany.util.HexUtils;

import javax.crypto.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * @author Soybeany
 * @date 2020/12/1
 */
public class Payload {

    private static final String ALGORITHM = "AES";
    private static final Charset CHARSET = StandardCharsets.UTF_8;
    private static final Gson GSON = new Gson();

    /**
     * 用于正式请求App服务器的fileId
     */
    public final String fileToken;

    public static SecretKey generateNewKey() throws Exception {
        return generateKey();
    }

    public static Payload fromString(String encryptedJson, SecretKey secretKey) throws Exception {
        String json = decrypt(encryptedJson, secretKey);
        return GSON.fromJson(json, Payload.class);
    }

    public static String fromPayload(Payload payload, SecretKey secretKey) throws Exception {
        String json = GSON.toJson(payload);
        return encrypt(json, secretKey);
    }

    public Payload(String fileToken) {
        this.fileToken = fileToken;
    }

    // ***********************内部方法****************************

    private static SecretKey generateKey() throws NoSuchAlgorithmException {
        KeyGenerator secretGenerator = KeyGenerator.getInstance(ALGORITHM);
        SecureRandom secureRandom = new SecureRandom();
        secretGenerator.init(secureRandom);
        return secretGenerator.generateKey();
    }

    private static String encrypt(String content, SecretKey secretKey) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        byte[] bytes = aes(content.getBytes(CHARSET), Cipher.ENCRYPT_MODE, secretKey);
        return HexUtils.bytesToHex(bytes);
    }

    private static String decrypt(String encryptedContent, SecretKey secretKey) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        byte[] contentArray = HexUtils.hexToByteArray(encryptedContent);
        byte[] result = aes(contentArray, Cipher.DECRYPT_MODE, secretKey);
        return new String(result, CHARSET);
    }

    private static byte[] aes(byte[] contentArray, int mode, SecretKey secretKey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(mode, secretKey);
        return cipher.doFinal(contentArray);
    }

}
