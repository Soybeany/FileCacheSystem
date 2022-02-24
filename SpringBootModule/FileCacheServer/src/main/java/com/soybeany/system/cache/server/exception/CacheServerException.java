package com.soybeany.system.cache.server.exception;

import com.soybeany.system.cache.core.exception.FileDownloadException;

/**
 * @author Soybeany
 * @date 2022/2/16
 */
public class CacheServerException extends FileDownloadException {
    public CacheServerException(String message) {
        super(500, message);
    }
}
