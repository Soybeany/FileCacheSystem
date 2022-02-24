package com.soybeany.system.cache.app.service;

import com.soybeany.system.cache.core.api.IDownloadInfoProvider;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Soybeany
 * @date 2022/2/19
 */
public interface DownloadService extends IDownloadInfoProvider {

    @SuppressWarnings("UnusedReturnValue")
    boolean download(HttpServletRequest request, HttpServletResponse response, String fileToken) throws IOException;

}
