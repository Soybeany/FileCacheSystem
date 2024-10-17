package com.soybeany.system.cache.server.config;

import com.soybeany.system.cache.core.dto.FileUid;


public interface IDynamicConfigProvider {

    ServerInfo getAppServer(FileUid fileUid);

    FileUid toFileUid(String token);

    int getDownloadTimeoutSeconds();

}
