package com.soybeany.system.cache.core.util;

import com.soybeany.system.cache.core.security.model.FcException;
import com.soybeany.util.HexUtils;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.MessageDigest;

public abstract class FileMd5Utils {

    public static final String HEADER_MD5 = "Content-MD5";
    private static final int BUFFER_SIZE = 10 * 1024 * 1024;
    private static final int MD5_SIZE = 16;
    private static final int AVAILABLE_LENGTH = BUFFER_SIZE - MD5_SIZE;

    public static String calculateMd5(File file) {
        try (InputStream is = Files.newInputStream(file.toPath())) {
            int length;
            byte[] buffer = new byte[BUFFER_SIZE], result = new byte[MD5_SIZE];
            while ((length = is.read(buffer, MD5_SIZE, AVAILABLE_LENGTH)) > 0) {
                // 将上一次的结果与新数据混合
                System.arraycopy(result, 0, buffer, 0, MD5_SIZE);
                // 开始计算混合后数据的md5
                MessageDigest digest = MessageDigest.getInstance("MD5");
                digest.update(buffer, 0, MD5_SIZE + length);
                result = digest.digest();
            }
            return HexUtils.bytesToHex(result);
        } catch (Exception e) {
            throw new FcException("MD5计算异常:" + e.getMessage());
        }
    }
}
