package com.soybeany.system.cache.server.service;

import com.soybeany.system.cache.core.model.FileUid;
import com.soybeany.system.cache.server.exception.CacheServerException;
import com.soybeany.system.cache.server.model.CacheFileInfo;

/**
 * 文件提供服务，入口服务
 *
 * @author Soybeany
 * @date 2022/2/16
 */
public interface FileManagerService {

    CacheFileInfo getFile(FileUid fileUid) throws CacheServerException;

    CacheFileInfo getFile(FileUid fileUid, int lockTimeoutSec) throws CacheServerException;

}
