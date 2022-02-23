package com.soybeany.system.cache.demo.app.controller;

import com.soybeany.rpc.provider.api.IRpcServiceExecutor;
import com.soybeany.sync.core.model.SyncDTO;
import com.soybeany.system.cache.app.DownloadService;
import com.soybeany.system.cache.app.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Soybeany
 * @date 2022/1/27
 */
@RestController
@RequestMapping("/api")
public class ApiController {

    @Autowired
    private DownloadService downloadService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private IRpcServiceExecutor invoker;

    @PostMapping("/rpc")
    public SyncDTO rpc(HttpServletRequest request, HttpServletResponse response) {
        return invoker.execute(request, response);
    }

    // ***********************缓存服务器调用****************************

    @GetMapping("/download")
    public void download(HttpServletRequest request, HttpServletResponse response, String fileToken) throws IOException {
        downloadService.download(request, response, fileToken);
    }

    // ***********************普通用户调用****************************

    @GetMapping("/token")
    public String getToken(HttpServletResponse response, String fileToken) {
        try {
            return taskService.getToken(fileToken);
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return null;
        }
    }

    // ***********************管理员调用****************************

    @PostMapping("/task")
    public String postTask(String fileToken) {
        taskService.postTask(fileToken);
        return "ok";
    }

}
