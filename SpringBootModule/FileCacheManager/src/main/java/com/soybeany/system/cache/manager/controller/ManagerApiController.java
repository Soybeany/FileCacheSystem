package com.soybeany.system.cache.manager.controller;

import com.soybeany.system.cache.core.dto.CacheTask;
import com.soybeany.system.cache.core.security.interfaces.FileCacheHttpContract.Dto;
import com.soybeany.system.cache.manager.service.FileUidService;
import com.soybeany.system.cache.manager.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.soybeany.system.cache.core.security.interfaces.FileCacheHttpContract.GET_SECRET_KEY_LIST;
import static com.soybeany.system.cache.core.security.interfaces.FileCacheHttpContract.POST_TASK_LIST;

/**
 * @author Soybeany
 * @date 2020/12/11
 */
@RestController
class ManagerApiController {

    @Autowired
    private FileUidService fileUidService;
    @Autowired
    private TaskService taskService;

    // ********************标准API********************

    @GetMapping(GET_SECRET_KEY_LIST)
    public Dto<String> getList() {
        try {
            return Dto.norm(fileUidService.getSecretKeyProvider().getHolderString());
        } catch (Exception e) {
            return Dto.error("获取失败:" + e.getMessage());
        }
    }

    @PostMapping(POST_TASK_LIST)
    Dto<String> ensure(@RequestBody List<CacheTask> tasks) {
        try {
            taskService.saveTasks(tasks);
            return Dto.success();
        } catch (Exception e) {
            return Dto.error(e.getMessage());
        }
    }

}
