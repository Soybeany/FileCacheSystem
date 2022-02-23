package com.soybeany.system.cache.demo.app.service;

import com.soybeany.download.FileServerUtils;
import com.soybeany.download.core.FileInfo;
import com.soybeany.system.cache.app.BaseDownloadServiceImpl;
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
public class DownloadServiceImpl extends BaseDownloadServiceImpl {

    private static final String DIR = "D:\\cache-test\\app";

    @Override
    protected String onSetupDownloadUrl() {
        return "http://localhost:8183/api/download";
    }

    @Override
    public void onDownload(HttpServletRequest request, HttpServletResponse response, String fileToken) throws IOException {
        File file = new File(DIR, fileToken);
        if (!file.exists()) {
            throw new FileNotFoundException();
        }
        FileInfo fileInfo = FileInfo.getNewAttachment(file.getName(), file.length(), file.lastModified() + "");
        response.setHeader("Age", "30");
        FileServerUtils.randomAccessDownloadFile(fileInfo, request, response, file);
    }

}
