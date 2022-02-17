package com.soybeany.system.cache.server.service;

import com.soybeany.system.cache.core.model.FileUid;
import com.soybeany.system.cache.server.exception.FileStorageException;

import java.io.File;

/**
 * @author Soybeany
 * @date 2022/2/16
 */
public interface FileStorageService {

    File loadFile(FileUid fileUid);

    File saveFile(FileUid fileUid, File tempFile) throws FileStorageException;

    String[] getServers();

    File[] getFiles(String server);

    String getTempFileDir();

    File loadTempFile(FileUid fileUid, String fileName);

}
