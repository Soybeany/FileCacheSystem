package com.soybeany.system.cache.server.service;

import com.soybeany.system.cache.core.security.interfaces.FileCacheHttpContract;
import com.soybeany.system.cache.core.security.model.PollingHostProvider;
import com.soybeany.system.cache.core.task.FileUid;
import com.soybeany.system.cache.server.config.AppConfig;
import com.soybeany.system.cache.server.config.ServerInfo;
import com.soybeany.system.cache.server.model.DataInfo;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
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
    private AppConfig appConfig;

    private final Map<String, ServerInfo> serverInfoMap = new HashMap<>();
    private OkHttpClient client;


    @Override
    public OkHttpClient getClient() {
        return client;
    }

    public DownloadInfo startDownload(FileUid fileUid) {
        ServerInfo serverInfo = getServerInfo(fileUid.server);
        String fileToken = fileUid.fileToken + (serverInfo.urlSuffix != null ? serverInfo.urlSuffix : "");
        Map<String, String> headers = new HashMap<>();
        headers.put(FileCacheHttpContract.AUTHORIZATION, serverInfo.authorization);
        Response response = getResponse(PollingHostProvider.fromArr(serverInfo.fileDownloadUrl), fileToken, headers);
        return new DownloadInfo(fromResponse(response), getNonNullBody(response.body()).byteStream());
    }

    @PostConstruct
    private void onInit() {
        for (ServerInfo serverInfo : appConfig.appServers) {
            serverInfoMap.put(serverInfo.name, serverInfo);
        }
        client = FileCacheHttpContract.getNewClient(appConfig.downloadTimeoutSec);
    }

    /**
     * 获取服务器配置信息
     */
    private ServerInfo getServerInfo(String server) {
        return Optional.ofNullable(serverInfoMap.get(server)).orElseThrow(() -> new RuntimeException("没有该服务器的相关信息"));
    }

    private DataInfo fromResponse(Response response) {
        DataInfo info = new DataInfo();
        info.eTag = response.header("ETag");
        info.pTtl = Optional.ofNullable(response.header("Age")).map(age -> Integer.parseInt(age) * 1000).orElse(DEFAULT_CACHE_AGE);
        info.contentType = response.header("Content-Type");
        info.contentLength = Optional.ofNullable(response.header("Content-Length")).map(Long::parseLong).orElse(null);
        info.contentDisposition = response.header("Content-Disposition");
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
