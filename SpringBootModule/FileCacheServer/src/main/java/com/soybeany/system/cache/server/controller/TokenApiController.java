package com.soybeany.system.cache.server.controller;

import com.soybeany.download.FileServerUtils;
import com.soybeany.system.cache.core.interfaces.FileCacheHttpContract;
import com.soybeany.system.cache.core.model.FileUid;
import com.soybeany.system.cache.core.token.Payload;
import com.soybeany.system.cache.core.token.TokenPart;
import com.soybeany.system.cache.server.model.CacheFileInfo;
import com.soybeany.system.cache.server.service.FileManagerService;
import com.soybeany.system.cache.server.service.TokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * @author Soybeany
 * @date 2020/11/30
 */
@Slf4j
@RestController
class TokenApiController {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private FileManagerService fileManagerService;

    @GetMapping(FileCacheHttpContract.GET_FILE_PATH + "/{token}")
    void getFile(@PathVariable String token, HttpServletRequest request, HttpServletResponse response) {
        try {
            TokenPart tokenPart = TokenPart.fromToken(token);
            Payload payload = tokenService.getPayload(tokenPart);
            CacheFileInfo fileInfo = fileManagerService.getFile(new FileUid(tokenPart.server, payload.fileToken));
            FileServerUtils.randomAccessDownloadFile(fileInfo, request, response, fileInfo.getFile());
        } catch (Exception e) {
            String uuid = UUID.randomUUID().toString();
            log.error(e.getMessage() + "(" + uuid + ")");
            if (response.isCommitted()) {
                return;
            }
            response.reset();
            response.setHeader("errMsg", uuid);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

}
