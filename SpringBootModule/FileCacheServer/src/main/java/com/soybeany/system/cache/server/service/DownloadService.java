package com.soybeany.system.cache.server.service;

import com.soybeany.system.cache.core.dto.FileUid;
import com.soybeany.system.cache.core.security.interfaces.FileCacheHttpContract;
import com.soybeany.system.cache.core.security.model.FcException;
import com.soybeany.system.cache.core.security.model.PollingHostProvider;
import com.soybeany.system.cache.core.util.FileMd5Utils;
import com.soybeany.system.cache.server.config.AppConfig;
import com.soybeany.system.cache.server.config.IDynamicConfigProvider;
import com.soybeany.system.cache.server.config.ServerInfo;
import com.soybeany.system.cache.server.model.DataInfo;
import com.soybeany.system.cache.server.model.RetryException;
import com.soybeany.system.cache.server.storage.FileCacheAccessor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

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

    private static final Logger LOG = LoggerFactory.getLogger(DownloadService.class);
    private static final long DEFAULT_CACHE_AGE = 5 * 24 * 60 * 60 * 1000;

    private final Map<String, BlockingQueue<Object>> tokenMap = new HashMap<>();

    @Autowired
    private AppConfig appConfig;
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
        // 尝试获得令牌
        Object token = getToken(fileUid);
        try {
            return startRequest(fileUid, serverInfo);
        } finally {
            releaseToken(fileUid, token);
        }
    }

    // ***********************内部方法****************************

    private FileCacheAccessor startRequest(FileUid fileUid, ServerInfo serverInfo) {
        String path = fileUid.fileId + (serverInfo.urlSuffix != null ? serverInfo.urlSuffix : "");
        while (true) {
            Map<String, String> headers = new HashMap<>();
            if (null != serverInfo.authorization) {
                headers.put(FileCacheHttpContract.HEADER_AUTHORIZATION, serverInfo.authorization);
            }
            if (null != fileUid.exInfo) {
                headers.put(FileCacheHttpContract.HEADER_EX_INFO, fileUid.exInfo);
            }
            downloadAppendService.beforeRequest(fileUid, headers);
            Response response = getResponse(PollingHostProvider.fromArr(serverInfo.fileDownloadUrl), path, headers);
            DataInfo dataInfo = toDataInfo(response);
            try {
                return downloadAppendService.getFileCacheAccessor(fileUid, response, dataInfo)
                        .orElseGet(() -> FileCacheAccessor.fromStream(dataInfo, () -> getNonNullBody(response.body()).byteStream()));
            } catch (RetryException e) {
                LOG.info(e.getMessage());
            }
        }
    }

    private Object getToken(FileUid fileUid) {
        BlockingQueue<Object> map = getQueue(fileUid);
        try {
            if (map.isEmpty()) {
                LOG.info("“" + fileUid.fileId + "”即将等待令牌");
            }
            long now = System.currentTimeMillis();
            Object token = map.poll(appConfig.concurrentWaitTimeoutSec, TimeUnit.SECONDS);
            boolean isSuccess = null != token;
            LOG.info("“" + fileUid.fileId + "”令牌获取" + (isSuccess ? "成功" : "失败") + "，耗时" + (System.currentTimeMillis() - now) + "ms");
            if (null == token) {
                throw new FcException("“" + fileUid.fileId + "”获取令牌超时");
            }
            return token;
        } catch (InterruptedException e) {
            throw new FcException(e);
        }
    }

    private synchronized BlockingQueue<Object> getQueue(FileUid fileUid) {
        return tokenMap.computeIfAbsent(fileUid.server, k -> {
            BlockingQueue<Object> queue = new LinkedBlockingQueue<>();
            for (int i = 0; i < appConfig.concurrentMaxConnections; i++) {
                queue.add(new Object());
            }
            return queue;
        });
    }

    private void releaseToken(FileUid fileUid, Object token) {
        BlockingQueue<Object> map = Optional.ofNullable(tokenMap.get(fileUid.server)).orElseThrow(() -> new FcException("使用了未知的令牌桶(" + fileUid.server + ")"));
        map.add(token);
    }

    private DataInfo toDataInfo(Response response) {
        DataInfo info = new DataInfo();
        info.eTag = response.header("ETag");
        info.pTtl = Optional.ofNullable(response.header("Age")).map(age -> Long.parseLong(age) * 1000).orElse(DEFAULT_CACHE_AGE);
        info.contentType = response.header("Content-Type");
        info.contentLength = Optional.ofNullable(response.header("Content-Length")).map(Long::parseLong).orElse(null);
        info.contentDisposition = response.header("Content-Disposition");
        info.md5 = response.header(FileMd5Utils.HEADER_MD5);
        info.exInfo = FileCacheHttpContract.decodeExInfo(response.header(FileCacheHttpContract.HEADER_EX_INFO));
        return info;
    }

}
