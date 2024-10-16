package com.soybeany.system.cache.server.config;

import com.soybeany.system.cache.core.dto.FileUid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
class LocalDynamicConfigImpl implements IDynamicConfigProvider {

    @Autowired
    private AppConfig appConfig;

    @Override
    public ServerInfo getAppServer(FileUid fileUid) {
        for (ServerInfo appServer : appConfig.appServers) {
            if (appServer.name.equals(fileUid.server)) {
                return appServer;
            }
        }
        throw new RuntimeException("没有该服务器的相关信息");
    }

    @Override
    public String getAuthorization() {
        return appConfig.authorization;
    }

    @Override
    public int getDownloadTimeoutSeconds() {
        return appConfig.downloadTimeoutSec;
    }
}
