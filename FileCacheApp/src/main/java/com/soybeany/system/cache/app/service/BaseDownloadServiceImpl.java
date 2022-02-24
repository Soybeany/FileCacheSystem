package com.soybeany.system.cache.app.service;

import com.soybeany.system.cache.core.api.FileCacheContract;
import com.soybeany.system.cache.core.model.CacheAppInfo;
import com.soybeany.util.file.BdFileUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

/**
 * @author Soybeany
 * @date 2022/2/23
 */
public abstract class BaseDownloadServiceImpl implements DownloadService {

    private static final String AUTHORIZATION = BdFileUtils.getUuid();

    @Override
    public CacheAppInfo getCacheAppInfo() {
        return new CacheAppInfo(onSetupDownloadUrl(), AUTHORIZATION);
    }

    @Override
    public boolean download(HttpServletRequest request, HttpServletResponse response, String fileToken) throws IOException {
        // 判断是否为合法用户
        if (!isLegalUser(request)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        // 下载文件
        onDownload(request, response, fileToken);
        return true;
    }

    protected abstract String onSetupDownloadUrl();

    protected abstract void onDownload(HttpServletRequest request, HttpServletResponse response, String fileToken) throws IOException;

    // ***********************内部方法****************************

    private boolean isLegalUser(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(FileCacheContract.AUTHORIZATION))
                .map(AUTHORIZATION::equals)
                .orElse(false);
    }

}
