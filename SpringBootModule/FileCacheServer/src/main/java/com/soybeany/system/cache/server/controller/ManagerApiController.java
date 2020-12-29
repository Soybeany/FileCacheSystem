package com.soybeany.system.cache.server.controller;

import com.soybeany.system.cache.core.interfaces.FileCacheHttpContract.Dto;
import com.soybeany.system.cache.core.model.CacheTask;
import com.soybeany.system.cache.server.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.soybeany.system.cache.core.interfaces.FileCacheHttpContract.POST_TASK_LIST;

/**
 * @author Soybeany
 * @date 2020/12/22
 */
@RestController
class ManagerApiController {

    @Autowired
    private TaskService taskService;

    @PostMapping(POST_TASK_LIST)
    Dto<String> postTaskList(@RequestBody List<CacheTask> tasks) {
        try {
            taskService.saveTasks(tasks);
            return Dto.success();
        } catch (Exception e) {
            return Dto.error(e.getMessage());
        }
    }
}
