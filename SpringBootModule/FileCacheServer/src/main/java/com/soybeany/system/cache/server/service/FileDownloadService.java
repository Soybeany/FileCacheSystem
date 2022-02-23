package com.soybeany.system.cache.server.service;

import com.soybeany.system.cache.core.model.FileUid;
import com.soybeany.system.cache.server.exception.FileDownloadException;
import com.soybeany.system.cache.server.model.CacheFileInfo;

/**
 * @author Soybeany
 * @date 2022/2/16
 */
public interface FileDownloadService {

    CacheFileInfo downloadFile(FileUid fileUid, String storageName) throws FileDownloadException;

}
