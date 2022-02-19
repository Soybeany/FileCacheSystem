package com.soybeany.system.cache.server.sync;

import com.soybeany.mq.consumer.api.IMqMsgHandler;
import com.soybeany.system.cache.core.api.FileCacheContract;
import com.soybeany.system.cache.core.model.CacheTask;
import com.soybeany.system.cache.server.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.List;

/**
 * @author Soybeany
 * @date 2022/1/26
 */
@Component
public class TaskHandler extends IMqMsgHandler.JsonMsg<CacheTask> {

    @Autowired
    private TaskService taskService;

    @Override
    public String onSetupTopic() {
        return FileCacheContract.TOPIC_TASK_LIST;
    }

    @Override
    protected void onHandleObjects(List<CacheTask> tasks) {
        taskService.saveTasks(tasks);
    }

    @Override
    protected Type onSetupObjType() {
        return CacheTask.class;
    }

}
