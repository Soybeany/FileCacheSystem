package com.soybeany.system.cache.server.model;

import com.soybeany.download.FileDownloadUtils;

import java.io.File;
import java.io.IOException;

/**
 * @author Soybeany
 * @date 2020/12/9
 */
public class CacheInfoWithFile extends CacheInfo implements FileDownloadUtils.InfoProvider {
    public final File file;
    private final String eTag;

    public CacheInfoWithFile(File file, String eTag) {
        this.file = file;
        this.eTag = eTag;
    }

    @Override
    public String getContentType() {
        return null != contentType ? contentType : "application/octet-stream";
    }

    @Override
    public long getContentLength() {
        return null != contentLength ? contentLength : file.length();
    }

    @Override
    public String getContentDisposition() throws IOException {
        return null != contentDisposition ? contentDisposition : FileDownloadUtils.getAttachmentContentDisposition(file.getName());
    }

    @Override
    public String getEtag() {
        return null != eTag ? eTag : file.lastModified() + "";
    }

    public void setContentType(String type) {
        contentType = type;
    }

    public void setContentLength(Long contentLength) {
        this.contentLength = contentLength;
    }

    public void setContentDisposition(String contentDisposition) {
        this.contentDisposition = contentDisposition;
    }
}
