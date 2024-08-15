package com.soybeany.system.cache.server.model;

import java.io.File;

/**
 * @author Soybeany
 * @date 2020/12/19
 */
public class DataInfo {

    public String eTag;
    public String contentType;
    public Long contentLength;
    public String contentDisposition;
    public Integer pTtl;

    public boolean isFileComplete(File file) {
        return null == contentLength || contentLength.equals(file.length());
    }

}
