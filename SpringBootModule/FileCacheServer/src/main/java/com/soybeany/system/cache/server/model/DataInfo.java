package com.soybeany.system.cache.server.model;

import com.soybeany.download.DataSupplier;
import com.soybeany.system.cache.core.security.model.FcException;
import com.soybeany.util.file.BdFileUtils;

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

    public void checkFileIntegrity(File file) {
        // 尝试校验文件长度
        long fileLength;
        if (null != contentLength && !contentLength.equals(fileLength = file.length())) {
            throw new FcException("文件长度不同(" + contentLength + " - " + fileLength + ")");
        }
        // 尝试校验文件md5
        String fileMd51, fileMd52;
        if (null != md5 && !(md5.equals(fileMd51 = DataSupplier.calMd5Old(file)) || md5.equals(fileMd52 = BdFileUtils.md5(file)))) {
            throw new FcException("文件md5不同(" + md5 + " - " + fileMd51 + "/" + fileMd52 + ")");
        }
    }

}
