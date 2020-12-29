package com.soybeany.system.cache.server.timer;

import com.soybeany.system.cache.server.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 任务执行，即主动缓存
 *
 * @author Soybeany
 * @date 2020/12/18
 */
@Component
class TaskExecuteTimer extends BaseTimer {

    @Autowired
    private TaskService taskService;

    @Override
    protected void onSignal() {
        taskService.findAndExecuteNewTasks();
    }
}
