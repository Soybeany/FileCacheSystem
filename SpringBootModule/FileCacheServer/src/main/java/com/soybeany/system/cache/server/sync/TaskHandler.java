package com.soybeany.system.cache.server.sync;

import com.soybeany.mq.consumer.api.IMqMsgHandler;
import com.soybeany.system.cache.core.api.FileCacheContract;
import com.soybeany.system.cache.core.model.CacheTask;
import com.soybeany.system.cache.server.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Soybeany
 * @date 2022/1/26
 */
@Component
public class TaskHandler implements IMqMsgHandler<CacheTask.WithStamp> {

    @Lazy
    @Autowired
    private TaskService taskService;

    @Override
    public String onSetupTopic() {
        return FileCacheContract.TOPIC_TASK_LIST;
    }

    @Override
    public void onHandle(List<CacheTask.WithStamp> tasks) {
        taskService.saveTasks(tasks);
    }

}
