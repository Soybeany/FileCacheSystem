package com.soybeany.system.cache.core.dto;

import com.soybeany.util.Md5Utils;

/**
 * 文件标签，确定唯一性
 *
 * @author Soybeany
 * @date 2020/12/20
 */
public class FileUid {

    private static final String SEPARATOR = "-";

    public final String server;
    public final String fileId;
    public String exInfo;

    @SuppressWarnings("unused")
    public static FileUid fromString(String fileUidStr) {
        String[] parts = fileUidStr.split(SEPARATOR);
        return new FileUid(parts[0], fileUidStr.substring(parts[0].length() + SEPARATOR.length()));
    }

    public static String toFileUid(String server, String fileToken) {
        return server + SEPARATOR + fileToken;
    }

    public static String toString(FileUid fileUid) {
        return toFileUid(fileUid.server, fileUid.fileId);
    }

    public FileUid(String server, String fileId) {
        this.server = server;
        this.fileId = fileId;
    }

    public String getKey() {
        String key = fileId.replaceAll("[/\\\\]", "-");
        if (key.length() > 200) {
            key = "(md5)" + Md5Utils.strToMd5(key);
        }
        return key;
    }
}
