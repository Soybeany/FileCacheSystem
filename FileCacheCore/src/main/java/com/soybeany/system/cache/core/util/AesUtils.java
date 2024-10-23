package com.soybeany.system.cache.core.util;

import com.soybeany.exception.BdRtException;
import com.soybeany.system.cache.core.security.model.FcException;
import com.soybeany.util.HexUtils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * @author Soybeany
 * @date 2020/12/1
 */
class AesUtils {

    private static final String ALGORITHM = "AES";
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    public static SecretKey generateKey() {
        try {
            KeyGenerator secretGenerator = KeyGenerator.getInstance(ALGORITHM);
            secretGenerator.init(new SecureRandom());
            return secretGenerator.generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new FcException(e);
        }
    }

    public static String encrypt(String content, SecretKey secretKey) {
        byte[] bytes = aes(content.getBytes(CHARSET), Cipher.ENCRYPT_MODE, secretKey);
        return HexUtils.bytesToHex(bytes);
    }

    public static String decrypt(String encryptedContent, SecretKey secretKey) {
        byte[] contentArray = HexUtils.hexToByteArray(encryptedContent);
        byte[] result = aes(contentArray, Cipher.DECRYPT_MODE, secretKey);
        return new String(result, CHARSET);
    }

    private static byte[] aes(byte[] contentArray, int mode, SecretKey secretKey) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(mode, secretKey);
            return cipher.doFinal(contentArray);
        } catch (Exception e) {
            throw new BdRtException(e.getMessage());
        }
    }

}
