package com.soybeany.system.cache.server.service;

import com.soybeany.system.cache.core.model.FileUid;
import com.soybeany.system.cache.server.exception.FileStorageException;
import com.soybeany.util.file.BdFileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * @author Soybeany
 * @date 2022/2/16
 */
@Service
public class FileStorageServiceImpl implements FileStorageService {

    @Autowired
    private ConfigService configService;

    @Override
    public File loadFile(FileUid fileUid, String storageName) {
        return getFile(fileUid, storageName);
    }

    @Override
    public File saveFile(FileUid fileUid, String storageName, File tempFile) throws FileStorageException {
        File file = getFile(fileUid, storageName);
        BdFileUtils.mkParentDirs(file);
        if (file.exists() && !file.delete()) {
            throw new FileStorageException("旧文件(" + file.getName() + ")无法删除");
        }
        if (!tempFile.renameTo(file)) {
            throw new FileStorageException("临时文件(" + tempFile.getName() + ") -> 新文件(" + file.getName() + ")转换失败");
        }
        return file;
    }

    @Override
    public String[] getServers() {
        return new File(getCacheFileDir()).list();
    }

    @Override
    public File[] getFiles(String server) {
        return new File(getCacheFileDir(), server).listFiles();
    }

    @Override
    public String getTempFileDir() {
        return configService.get().getTempFileDir();
    }

    @Override
    public File loadTempFile(String fileName) {
        return new File(getTempFileDir(), fileName);
    }

    // ***********************内部方法****************************

    private File getFile(FileUid fileUid, String storageName) {
        String fileName = (null != storageName ? storageName : fileUid.fileToken);
        return new File(getCacheFileDir() + File.separator + fileUid.server, fileName);
    }

    private String getCacheFileDir() {
        return configService.get().getCacheFileDir();
    }

}
