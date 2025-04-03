package com.soybeany.system.cache.server.model;

import com.soybeany.download.DataSupplier;
import com.soybeany.download.core.Md5Type;
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
    public Md5Type md5Type;
    public String md5;
    public String exInfo;

    public void checkFileIntegrity(File file) {
        // 尝试校验文件长度
        long fileLength;
        if (null != contentLength && !contentLength.equals(fileLength = file.length())) {
            throw new FcException("文件长度不同(" + contentLength + " - " + fileLength + ")");
        }
        // 尝试校验文件md5
        if (null != md5) {
            String fileMd5;
            switch (md5Type) {
                case OLD:
                    fileMd5 = DataSupplier.calMd5Old(file);
                    break;
                case STD:
                    fileMd5 = BdFileUtils.md5(file);
                    break;
                default:
                    throw new FcException("使用了不支持的md5Type(" + md5Type + ")");
            }
            if (!md5.equals(fileMd5)) {
                throw new FcException("文件md5不同(" + md5 + " - " + fileMd5 + ")");
            }
        }
    }

}
