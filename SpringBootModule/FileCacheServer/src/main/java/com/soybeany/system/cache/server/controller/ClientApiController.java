package com.soybeany.system.cache.server.controller;

import com.soybeany.system.cache.core.security.interfaces.FileCacheHttpContract;
import com.soybeany.system.cache.server.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Soybeany
 * @date 2020/11/30
 */
@RestController
class ClientApiController {

    @Autowired
    private ManageService manageService;

    @GetMapping("/ensure/{token}")
    String ensure(@PathVariable String token, HttpServletRequest request, HttpServletResponse response) {
        return manageService.onEnsure(token, request, response);
    }

    @GetMapping(FileCacheHttpContract.GET_FILE_PATH + "/{token}")
    void getFile(@PathVariable String token, HttpServletRequest request, HttpServletResponse response) {
        manageService.onDownload(token, request, response);
    }

}
