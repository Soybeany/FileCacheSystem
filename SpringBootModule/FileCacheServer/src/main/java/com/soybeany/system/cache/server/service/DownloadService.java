package com.soybeany.system.cache.server.service;

import com.soybeany.download.core.BdDownloadHeaders;
import com.soybeany.download.core.Md5Type;
import com.soybeany.system.cache.core.dto.FileUid;
import com.soybeany.system.cache.core.security.interfaces.FcHeaders;
import com.soybeany.system.cache.core.security.interfaces.FileCacheHttpContract;
import com.soybeany.system.cache.core.security.model.FcException;
import com.soybeany.system.cache.core.security.model.PollingHostProvider;
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
// todo 1.获取全列表 2.查询指定的FileUid处于哪种状态：任务队列；令牌等待(等待下载)、下载中；已下载；没有
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

    public boolean isNotModified(FileUid fileUid, FileCacheAccessor.Local accessor) {
        ServerInfo serverInfo = configProvider.getAppServer(fileUid);
        // 检查本地文件是否有被修改
        try {
            accessor.dataInfo.checkFileIntegrity(accessor.file());
        } catch (Exception e) {
            LOG.warn("本地源文件检测异常: {}", e.getMessage());
            return false;
        }
        // 检查远端源文件是否有更新
        try (Response response = getResponse(configProvider.getCheckTimeoutSeconds(), serverInfo, getPath(fileUid, serverInfo), getHeaders(fileUid, serverInfo))) {
            return accessor.dataInfo.eTag.equals(response.header(BdDownloadHeaders.E_TAG));
        } catch (Exception e) {
            LOG.warn("远端源文件更新检测异常: {}", e.getMessage());
            return true;
        }
    }

    // ***********************内部方法****************************

    private FileCacheAccessor startRequest(FileUid fileUid, ServerInfo serverInfo) {
        String path = getPath(fileUid, serverInfo);
        while (true) {
            Map<String, String> headers = getHeaders(fileUid, serverInfo);
            downloadAppendService.beforeRequest(fileUid, headers);
            Response response = getResponse(configProvider.getDownloadTimeoutSeconds(), serverInfo, path, headers);
            DataInfo dataInfo = toDataInfo(response);
            try {
                return downloadAppendService.getFileCacheAccessor(fileUid, response, dataInfo)
                        .orElseGet(() -> FileCacheAccessor.fromStream(dataInfo, () -> getNonNullBody(response.body()).byteStream()));
            } catch (RetryException e) {
                LOG.info(e.getMessage());
            }
        }
    }

    private Response getResponse(int timeoutSec, ServerInfo serverInfo, String path, Map<String, String> headers) {
        OkHttpClient client = FileCacheHttpContract.getNewClient(timeoutSec);
        return getResponse(client, PollingHostProvider.fromArr(serverInfo.fileDownloadUrl), path, headers);
    }

    private String getPath(FileUid fileUid, ServerInfo serverInfo) {
        return fileUid.fileId + (serverInfo.urlSuffix != null ? serverInfo.urlSuffix : "");
    }

    private Map<String, String> getHeaders(FileUid fileUid, ServerInfo serverInfo) {
        Map<String, String> headers = new HashMap<>();
        if (null != serverInfo.authorization) {
            headers.put(FcHeaders.AUTHORIZATION, serverInfo.authorization);
        }
        if (null != fileUid.exInfo) {
            headers.put(FcHeaders.EX_INFO, fileUid.exInfo);
        }
        headers.put(FcHeaders.MD5_TYPE, Md5Type.STD.name());
        return headers;
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
            throw new FcException("令牌获取异常:" + e.getMessage());
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
        info.eTag = response.header(BdDownloadHeaders.E_TAG);
        info.pTtl = Optional.ofNullable(response.header(BdDownloadHeaders.AGE)).map(age -> Long.parseLong(age) * 1000).orElse(DEFAULT_CACHE_AGE);
        info.contentType = response.header(BdDownloadHeaders.CONTENT_TYPE);
        info.contentLength = Optional.ofNullable(response.header(BdDownloadHeaders.CONTENT_LENGTH)).map(Long::parseLong).orElse(null);
        info.contentDisposition = response.header(BdDownloadHeaders.CONTENT_DISPOSITION);
        info.md5Type = Optional.ofNullable(response.header(FcHeaders.MD5_TYPE)).map(Md5Type::valueOf).orElse(null);
        info.md5 = response.header(BdDownloadHeaders.CONTENT_MD5);
        info.exInfo = FileCacheHttpContract.decodeExInfo(response.header(FcHeaders.EX_INFO));
        return info;
    }

}
