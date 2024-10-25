package com.soybeany.system.cache.server.service;

import com.soybeany.system.cache.core.dto.FileUid;
import com.soybeany.system.cache.core.security.interfaces.FileCacheHttpContract;
import com.soybeany.system.cache.core.security.model.PollingHostProvider;
import com.soybeany.system.cache.core.util.ExInfoUtils;
import com.soybeany.system.cache.core.util.FileMd5Utils;
import com.soybeany.system.cache.server.config.IDynamicConfigProvider;
import com.soybeany.system.cache.server.config.ServerInfo;
import com.soybeany.system.cache.server.model.DataInfo;
import com.soybeany.system.cache.server.storage.FileCacheAccessor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * * 检查有没临时文件
 * - 有，走断点续传
 * - 无，普通请求
 * * 响应里的content-type是否大于某一个值
 * - 是，创建临时文件，走断点续传
 * - 没/否，直接下载
 * 只要响应初期正常，则认为是正常（不缓存异常，只在当次报错），也就是canAccess；否则，缓存异常
 *
 * @author Soybeany
 * @date 2020/12/1
 */
@Service
public class DownloadService implements FileCacheHttpContract {

    private static final long DEFAULT_CACHE_AGE = 5 * 24 * 60 * 60 * 1000;

    @Autowired
    private IDynamicConfigProvider configProvider;
    @Autowired
    private DownloadAppendService downloadAppendService;

    @Override
    public OkHttpClient getClient() {
        return FileCacheHttpContract.getNewClient(configProvider.getDownloadTimeoutSeconds());
    }

    public FileCacheAccessor startDownload(FileUid fileUid) {
        ServerInfo serverInfo = configProvider.getAppServer(fileUid);
        Map<String, String> headers = new HashMap<>();
        if (null != serverInfo.authorization) {
            headers.put(FileCacheHttpContract.AUTHORIZATION, serverInfo.authorization);
        }
        downloadAppendService.beforeRequest(fileUid, headers);
        String fileToken = fileUid.fileId + (serverInfo.urlSuffix != null ? serverInfo.urlSuffix : "");
        Response response = getResponse(PollingHostProvider.fromArr(serverInfo.fileDownloadUrl), fileToken, headers);
        DataInfo dataInfo = fromResponse(response);
        return downloadAppendService.getFileCacheAccessor(fileUid, response, dataInfo)
                .orElseGet(() -> FileCacheAccessor.fromStream(dataInfo, () -> getNonNullBody(response.body()).byteStream()));
    }

    private DataInfo fromResponse(Response response) {
        DataInfo info = new DataInfo();
        info.eTag = response.header("ETag");
        info.pTtl = Optional.ofNullable(response.header("Age")).map(age -> Long.parseLong(age) * 1000).orElse(DEFAULT_CACHE_AGE);
        info.contentType = response.header("Content-Type");
        info.contentLength = Optional.ofNullable(response.header("Content-Length")).map(Long::parseLong).orElse(null);
        info.contentDisposition = response.header("Content-Disposition");
        info.md5 = response.header(FileMd5Utils.HEADER_MD5);
        info.exInfo = ExInfoUtils.decodeExInfo(response.header(ExInfoUtils.HEADER_EX_INFO));
        return info;
    }

}
