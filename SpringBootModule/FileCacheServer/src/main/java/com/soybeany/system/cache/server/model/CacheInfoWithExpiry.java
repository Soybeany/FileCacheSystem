package com.soybeany.system.cache.server.model;

import okhttp3.Response;

/**
 * @author Soybeany
 * @date 2020/12/19
 */
public class CacheInfoWithExpiry extends CacheInfo {
    private Long expirySec;

    public static CacheInfoWithExpiry fromResponse(Response response) {
        CacheInfoWithExpiry info = new CacheInfoWithExpiry();
        info.eTag = response.header("ETag");
        info.setExpirySec(response.header("Age"));
        info.contentType = response.header("Content-Type");
        info.setContentLength(response.header("Content-Length"));
        info.contentDisposition = response.header("Content-Disposition");
        return info;
    }

    public Long getExpirySec() {
        return expirySec;
    }

    public void setExpirySec(String expirySec) {
        this.expirySec = (null != expirySec ? Long.parseLong(expirySec) : null);
    }

    public String getEtag() {
        return eTag;
    }

    public String getContentType() {
        return contentType;
    }

    public Long getContentLength() {
        return contentLength;
    }

    public void setContentLength(String contentLength) {
        this.contentLength = (null != contentLength ? Long.parseLong(contentLength) : null);
    }

    public String getContentDisposition() {
        return contentDisposition;
    }

}
