package com.soybeany.system.cache.server.controller;

import com.soybeany.download.FileServerUtils;
import com.soybeany.rpc.consumer.api.IRpcServiceProxy;
import com.soybeany.rpc.consumer.model.RpcProxySelector;
import com.soybeany.system.cache.core.api.IDownloadInfoProvider;
import com.soybeany.system.cache.core.exception.FileDownloadException;
import com.soybeany.system.cache.core.model.FileUid;
import com.soybeany.system.cache.core.model.Payload;
import com.soybeany.system.cache.core.model.TokenPart;
import com.soybeany.system.cache.server.model.CacheFileInfo;
import com.soybeany.system.cache.server.service.FileManagerService;
import com.soybeany.system.cache.server.service.TokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * @author Soybeany
 * @date 2020/11/30
 */
@Slf4j
@RestController
class ApiController {

    @Autowired
    private TokenService tokenService;
    @Autowired
    private IRpcServiceProxy rpcServiceProxy;
    @Autowired
    private FileManagerService fileManagerService;

    private RpcProxySelector<IDownloadInfoProvider> downloadInfoSelector;

    @GetMapping("/api/file/{token}")
    public void downloadByToken(@PathVariable String token, HttpServletRequest request, HttpServletResponse response) throws IOException {
        download(request, response, () -> {
            TokenPart tokenPart = TokenPart.fromToken(token);
            Payload payload = tokenService.getPayload(tokenPart);
            return new FileUid(tokenPart.server, payload.fileToken);
        });
    }

    @GetMapping("/download/{server}")
    public void downloadByCdn(@PathVariable String server, HttpServletRequest request, HttpServletResponse response) throws IOException {
        download(request, response, () -> {
            Map<String, String[]> params = request.getParameterMap();
            Map<String, List<String>> headers = getHeaders(request);
            String fileToken = downloadInfoSelector.get(server).getFileToken(headers, params);
            return new FileUid(server, fileToken);
        });
    }

    // ***********************内部方法****************************

    @PostConstruct
    private void onInit() {
        downloadInfoSelector = rpcServiceProxy.getSelector(IDownloadInfoProvider.class);
    }

    private Map<String, List<String>> getHeaders(HttpServletRequest request) {
        Map<String, List<String>> headers = new HashMap<>();
        Enumeration<String> headerNamesEnum = request.getHeaderNames();
        while (headerNamesEnum.hasMoreElements()) {
            String name = headerNamesEnum.nextElement();
            List<String> valueList = new ArrayList<>();
            Enumeration<String> valueEnum = request.getHeaders(name);
            while (valueEnum.hasMoreElements()) {
                valueList.add(valueEnum.nextElement());
            }
            headers.put(name, valueList);
        }
        return headers;
    }

    private void download(HttpServletRequest request, HttpServletResponse response, Callable<FileUid> fileUidProvider) throws IOException {
        try {
            CacheFileInfo fileInfo = fileManagerService.getFile(fileUidProvider.call());
            FileServerUtils.randomAccessDownloadFile(fileInfo, request, response, fileInfo.getFile());
        } catch (Exception e) {
            String uuid = UUID.randomUUID().toString();
            log.error(e.getMessage() + "(" + uuid + ")");
            if (response.isCommitted()) {
                return;
            }
            response.reset();
            // 特殊处理
            if (e instanceof FileDownloadException) {
                response.setContentType("text/plain; charset=UTF-8");
                response.setStatus(((FileDownloadException) e).getCode());
                response.getWriter().println(e.getMessage());
                return;
            }
            // 常规处理
            response.setHeader("errMsg", uuid);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

}
