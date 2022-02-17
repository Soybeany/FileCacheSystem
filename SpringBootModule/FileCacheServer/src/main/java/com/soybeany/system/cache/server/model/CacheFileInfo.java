package com.soybeany.system.cache.server.model;

import com.soybeany.download.core.FileInfo;
import lombok.Getter;

import java.io.File;

/**
 * @author Soybeany
 * @date 2022/2/16
 */
@Getter
public class CacheFileInfo extends FileInfo {

    private final int age;
    private final File file;

    public CacheFileInfo(String contentDisposition, long contentLength, String eTag, int age, File file) {
        super(contentDisposition, contentLength, eTag);
        this.age = age;
        this.file = file;
    }
}
