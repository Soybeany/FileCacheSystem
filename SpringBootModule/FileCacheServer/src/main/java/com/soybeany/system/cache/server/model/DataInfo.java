package com.soybeany.system.cache.server.model;

import com.soybeany.download.DataSupplier;
import com.soybeany.download.core.Md5Type;
import com.soybeany.system.cache.core.security.model.FcException;
import com.soybeany.util.file.BdFileUtils;

import java.io.File;
import java.util.Optional;

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

    public static String calMd5(File file, Md5Type md5Type) {
        switch (md5Type) {
            case OLD:
                return DataSupplier.calMd5Old(file);
            case STD:
                return BdFileUtils.md5(file);
            default:
                throw new FcException("使用了不支持的md5Type(" + md5Type + ")");
        }
    }

    public void checkFileIntegrity(File file) {
        // 尝试校验文件长度
        long fileLength;
        if (null != contentLength && !contentLength.equals(fileLength = file.length())) {
            throw new FcException("文件长度不同(" + contentLength + " - " + fileLength + ")");
        }
        // 尝试校验文件md5
        if (null != md5) {
            String fileMd5 = calMd5(file, getNotNullMd5Type());
            if (!md5.equals(fileMd5)) {
                throw new FcException("文件md5不同(" + md5 + " - " + fileMd5 + ")");
            }
        }
    }

    public boolean upgradeMd5(File file) {
        // 若已是新版，则不用处理
        if (!(Md5Type.OLD.equals(getNotNullMd5Type()))) {
            return false;
        }
        // 重新计算
        md5Type = Md5Type.STD;
        md5 = calMd5(file, Md5Type.STD);
        return true;
    }

    // ***********************内部方法****************************

    private Md5Type getNotNullMd5Type() {
        return Optional.ofNullable(md5Type).orElse(Md5Type.OLD);
    }

}
