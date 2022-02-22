package com.soybeany.system.cache.demo.manager.controller;

import com.soybeany.rpc.provider.api.IRpcServiceExecutor;
import com.soybeany.sync.core.model.SyncDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Soybeany
 * @date 2022/2/17
 */
@RestController
@RequestMapping("/sync")
public class SyncController {

    @Autowired
    private IRpcServiceExecutor invoker;

    @PostMapping("/rpc")
    SyncDTO rpc(HttpServletRequest request, HttpServletResponse response) {
        return invoker.execute(request, response);
    }

}
