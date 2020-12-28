package com.soybeany.system.cache.server.model;

import java.io.File;

/**
 * @author Soybeany
 * @date 2020/12/19
 */
public class CacheInfo {

    protected String eTag;
    protected String contentType;
    protected Long contentLength;
    protected String contentDisposition;

    public static boolean isFileComplete(Long contentLength, File file) {
        return null == contentLength || contentLength.equals(file.length());
    }

}
