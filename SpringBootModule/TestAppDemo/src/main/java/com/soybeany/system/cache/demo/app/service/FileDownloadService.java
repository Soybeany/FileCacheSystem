package com.soybeany.system.cache.demo.app.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Soybeany
 * @date 2022/2/19
 */
public interface FileDownloadService {

    void download(HttpServletRequest request, HttpServletResponse response, String fileToken) throws IOException;

}
