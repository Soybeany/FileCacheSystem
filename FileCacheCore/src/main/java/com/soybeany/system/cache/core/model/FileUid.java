package com.soybeany.system.cache.core.model;

/**
 * 文件标签，确定唯一性
 *
 * @author Soybeany
 * @date 2020/12/20
 */
public class FileUid {

    private static final String SEPARATOR = "-";

    public final String server;
    public final String fileToken;

    public static FileUid fromString(String string) {
        int separatorIndex = string.indexOf(SEPARATOR);
        String server = string.substring(0, separatorIndex);
        String fileUid = string.substring(separatorIndex + SEPARATOR.length());
        return new FileUid(server, fileUid);
    }

    public static String toFileUid(String server, String fileToken) {
        return server + SEPARATOR + fileToken;
    }

    public static String toString(FileUid fileUid) {
        return toFileUid(fileUid.server, fileUid.fileToken);
    }

    public FileUid(String server, String fileToken) {
        this.server = server;
        this.fileToken = fileToken;
    }
}
