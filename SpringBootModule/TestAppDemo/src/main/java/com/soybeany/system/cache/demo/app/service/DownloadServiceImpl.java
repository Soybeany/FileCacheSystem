package com.soybeany.system.cache.demo.app.service;

import com.soybeany.download.FileServerUtils;
import com.soybeany.download.core.BdDownloadException;
import com.soybeany.download.core.FileInfo;
import com.soybeany.system.cache.app.service.BaseDownloadServiceImpl;
import com.soybeany.system.cache.core.exception.FileDownloadException;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
            throw new BdDownloadException("指定的文件“" + fileToken + "”不存在");
        }
        FileInfo fileInfo = FileInfo.getNewAttachment(file.getName(), file.length(), file.lastModified() + "");
        response.setHeader("Age", "30");
        FileServerUtils.randomAccessDownloadFile(fileInfo, request, response, file);
    }

    @Override
    public String getFileToken(Map<String, List<String>> headers, Map<String, String[]> params) throws FileDownloadException {
        return Optional.ofNullable(params.get("fileToken"))
                .map(values -> values.length > 0 ? values[0] : null)
                .orElseThrow(() -> new FileDownloadException(400, "缺少入参"));
    }

}
