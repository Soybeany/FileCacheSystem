package com.soybeany.system.cache.server.model;

import com.soybeany.system.cache.core.util.FileMd5Utils;

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
    public String md5;

    public boolean isFileComplete(File file) {
        boolean isComplete = true;
        // 尝试校验文件长度
        if (null != contentLength) {
            isComplete &= contentLength.equals(file.length());
        }
        // 尝试校验文件md5
        if (null != md5) {
            isComplete &= md5.equals(FileMd5Utils.calculateMd5(file));
        }
        return isComplete;
    }

}
