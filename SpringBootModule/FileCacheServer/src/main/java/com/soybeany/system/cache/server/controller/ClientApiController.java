package com.soybeany.system.cache.server.controller;

import com.soybeany.download.FileServerUtils;
import com.soybeany.download.core.FileInfo;
import com.soybeany.system.cache.core.interfaces.FileCacheHttpContract;
import com.soybeany.system.cache.core.model.FileUid;
import com.soybeany.system.cache.core.token.Payload;
import com.soybeany.system.cache.core.token.TokenPart;
import com.soybeany.system.cache.server.service.CacheInfoService;
import com.soybeany.system.cache.server.service.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.UUID;

/**
 * @author Soybeany
 * @date 2020/11/30
 */
@RestController
class ClientApiController {

    private static final Logger LOG = LoggerFactory.getLogger(ClientApiController.class);

    @Autowired
    private TokenService tokenService;

    @Autowired
    private CacheInfoService cacheInfoService;

    @GetMapping("/ensure/{token}")
    String ensure(@PathVariable String token, HttpServletResponse response) {
        return handleContentInfo(token, response, (fileInfo, file) -> "ok", e -> "exception");
    }

    @GetMapping(FileCacheHttpContract.GET_FILE_PATH + "/{token}")
    void getFile(@PathVariable String token, HttpServletRequest request, HttpServletResponse response) {
        handleContentInfo(token, response, (fileInfo, file) -> {
            FileServerUtils.randomAccessDownloadFile(fileInfo, request, response, file);
            return null;
        }, e -> null);
    }

    private <T> T handleContentInfo(String token, HttpServletResponse response, IListener<T> callback, IExceptionHandler<T> handler) {
        try {
            TokenPart tokenPart = TokenPart.fromToken(token);
            Payload payload = tokenService.getPayload(tokenPart);
            FileUid fileUid = new FileUid(tokenPart.server, payload.fileId);
            FileInfo fileInfo = cacheInfoService.getCacheInfo(fileUid);
            File file = cacheInfoService.getDataFile(fileUid);
            return callback.onReceiveCacheInfo(fileInfo, file);
        } catch (Exception e) {
            String uuid = UUID.randomUUID().toString();
            LOG.error(e.getMessage() + "(" + uuid + ")");
            if (response.isCommitted()) {
                return null;
            }
            response.reset();
            response.setHeader("errMsg", uuid);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return handler.onHandleException(e);
        }
    }

    private interface IListener<T> {
        T onReceiveCacheInfo(FileInfo fileInfo, File file) throws Exception;
    }

    private interface IExceptionHandler<T> {
        T onHandleException(Exception e);
    }

}
