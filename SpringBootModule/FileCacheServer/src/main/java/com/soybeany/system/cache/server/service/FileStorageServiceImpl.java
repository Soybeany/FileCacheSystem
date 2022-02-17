package com.soybeany.system.cache.server.service;

import com.soybeany.system.cache.core.model.FileUid;
import com.soybeany.system.cache.server.exception.FileStorageException;
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
    public File loadFile(FileUid fileUid) {
        return getFile(fileUid);
    }

    @Override
    public File saveFile(FileUid fileUid, File tempFile) throws FileStorageException {
        File file = getFile(fileUid);
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
    public File loadTempFile(FileUid fileUid, String fileName) {
        return new File(getFile(fileUid), fileName);
    }

    // ***********************内部方法****************************

    private File getFile(FileUid fileUid) {
        return new File(getCacheFileDir() + File.separator + fileUid.server, fileUid.fileToken);
    }

    private String getCacheFileDir() {
        return configService.get().getCacheFileDir();
    }

}
