package com.soybeany.system.cache.server.exception;

/**
 * @author Soybeany
 * @date 2022/2/16
 */
public class FileNotReadyException extends CacheServerException {
    public FileNotReadyException() {
        super("文件未准备完毕");
    }
}
