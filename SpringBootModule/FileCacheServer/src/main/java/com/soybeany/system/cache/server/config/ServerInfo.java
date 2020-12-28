package com.soybeany.system.cache.server.config;

/**
 * @author Soybeany
 * @date 2020/11/30
 */
public class ServerInfo {

    public String name;
    public String fileDownloadUrl;
    public String authorization;

    public void setName(String name) {
        this.name = name;
    }

    public void setFileDownloadUrl(String fileDownloadUrl) {
        this.fileDownloadUrl = fileDownloadUrl;
    }

    public void setAuthorization(String authorization) {
        this.authorization = authorization;
    }
}
