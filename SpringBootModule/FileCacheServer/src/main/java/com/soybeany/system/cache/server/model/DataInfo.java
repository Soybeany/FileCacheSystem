package com.soybeany.system.cache.server.model;

import com.soybeany.download.DataSupplier;

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
    public Long pTtl;
    public String md5;
    public String exInfo;

    public boolean isFileComplete(File file) {
        boolean isComplete = true;
        // 尝试校验文件长度
        if (null != contentLength) {
            isComplete &= contentLength.equals(file.length());
        }
        // 尝试校验文件md5
        if (null != md5) {
            isComplete &= md5.equals(DataSupplier.calMd5Old(file));
        }
        return isComplete;
    }

}
