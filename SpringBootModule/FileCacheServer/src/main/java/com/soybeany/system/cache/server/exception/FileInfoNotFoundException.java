package com.soybeany.system.cache.server.exception;

/**
 * @author Soybeany
 * @date 2022/2/16
 */
public class FileInfoNotFoundException extends CacheServerException {
    public FileInfoNotFoundException() {
        super("找不到指定文件的信息");
    }
}
