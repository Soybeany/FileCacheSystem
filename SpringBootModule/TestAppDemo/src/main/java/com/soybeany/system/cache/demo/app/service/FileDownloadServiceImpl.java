package com.soybeany.system.cache.demo.app.service;

import com.soybeany.download.FileServerUtils;
import com.soybeany.download.core.FileInfo;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author Soybeany
 * @date 2022/2/19
 */
@Service
public class FileDownloadServiceImpl implements FileDownloadService {

    private static final String DIR = "D:\\cache-test\\app";

    @Override
    public void download(HttpServletRequest request, HttpServletResponse response, String fileToken) throws IOException {
        File file = new File(DIR, fileToken);
        if (!file.exists()) {
            throw new FileNotFoundException();
        }
        FileInfo fileInfo = FileInfo.getNewAttachment(file.getName(), file.length(), file.lastModified() + "");
        FileServerUtils.randomAccessDownloadFile(fileInfo, request, response, file);
    }

}
