package com.soybeany.system.cache.server.service;

import com.soybeany.system.cache.core.model.FileUid;
import com.soybeany.system.cache.server.exception.FileStorageException;

import java.io.File;

/**
 * @author Soybeany
 * @date 2022/2/16
 */
public interface FileStorageService {

    File loadFile(FileUid fileUid, String storageName);

    File saveFile(FileUid fileUid, String storageName, File tempFile) throws FileStorageException;

    /**
     * 获取当前本地有哪些服务器目录
     */
    String[] getServers();

    /**
     * 获取指定服务器目录下的全部文件
     */
    File[] getFiles(String server);

    String getTempFileDir();

    File loadTempFile(String fileName);

}
