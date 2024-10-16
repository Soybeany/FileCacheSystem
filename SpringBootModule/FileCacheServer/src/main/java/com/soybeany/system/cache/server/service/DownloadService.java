package com.soybeany.system.cache.server.service;

import com.soybeany.system.cache.core.dto.FileUid;
import com.soybeany.system.cache.core.security.interfaces.FileCacheHttpContract;
import com.soybeany.system.cache.core.security.model.PollingHostProvider;
import com.soybeany.system.cache.core.util.ExInfoUtils;
import com.soybeany.system.cache.core.util.FileMd5Utils;
import com.soybeany.system.cache.server.config.IDynamicConfigProvider;
import com.soybeany.system.cache.server.config.ServerInfo;
import com.soybeany.system.cache.server.model.DataInfo;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Soybeany
 * @date 2020/12/1
 */
@Service
public class DownloadService implements FileCacheHttpContract {

    private static final int DEFAULT_CACHE_AGE = 5 * 24 * 60 * 60 * 1000;

    @Autowired
    private IDynamicConfigProvider configProvider;

    @Override
    public OkHttpClient getClient() {
        return FileCacheHttpContract.getNewClient(configProvider.getDownloadTimeoutSeconds());
    }

    public DownloadInfo startDownload(FileUid fileUid) {
        ServerInfo serverInfo = configProvider.getAppServer(fileUid);
        String fileToken = fileUid.fileId + (serverInfo.urlSuffix != null ? serverInfo.urlSuffix : "");
        Map<String, String> headers = new HashMap<>();
        if (null != serverInfo.authorization) {
            headers.put(FileCacheHttpContract.AUTHORIZATION, serverInfo.authorization);
        }
        Response response = getResponse(PollingHostProvider.fromArr(serverInfo.fileDownloadUrl), fileToken, headers);
        return new DownloadInfo(fromResponse(response), getNonNullBody(response.body()).byteStream());
    }

    private DataInfo fromResponse(Response response) {
        DataInfo info = new DataInfo();
        info.eTag = response.header("ETag");
        info.pTtl = Optional.ofNullable(response.header("Age")).map(age -> Integer.parseInt(age) * 1000).orElse(DEFAULT_CACHE_AGE);
        info.contentType = response.header("Content-Type");
        info.contentLength = Optional.ofNullable(response.header("Content-Length")).map(Long::parseLong).orElse(null);
        info.contentDisposition = response.header("Content-Disposition");
        info.md5 = response.header(FileMd5Utils.HEADER_MD5);
        info.exInfo = ExInfoUtils.decodeExInfo(response.header(ExInfoUtils.HEADER_EX_INFO));
        return info;
    }

    public static class DownloadInfo {
        public final DataInfo info;
        public final InputStream is;

        public DownloadInfo(DataInfo info, InputStream is) {
            this.info = info;
            this.is = is;
        }
    }

}
