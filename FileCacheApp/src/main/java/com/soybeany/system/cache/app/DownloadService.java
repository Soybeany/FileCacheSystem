package com.soybeany.system.cache.app;

import com.soybeany.system.cache.core.api.ICacheAppInfoProvider;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Soybeany
 * @date 2022/2/19
 */
public interface DownloadService extends ICacheAppInfoProvider {

    @SuppressWarnings("UnusedReturnValue")
    boolean download(HttpServletRequest request, HttpServletResponse response, String fileToken) throws IOException;

}
