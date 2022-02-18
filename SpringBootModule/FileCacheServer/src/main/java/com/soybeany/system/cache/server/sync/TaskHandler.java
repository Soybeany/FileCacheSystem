package com.soybeany.system.cache.server.sync;

import com.google.gson.reflect.TypeToken;
import com.soybeany.mq.consumer.api.IMqMsgHandler;
import com.soybeany.system.cache.core.api.FileCacheHttpContract;
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
public class TaskHandler extends IMqMsgHandler.JsonMsg<List<CacheTask>> {

    private static final Type TYPE = new TypeToken<List<CacheTask>>() {
    }.getType();

    @Autowired
    private TaskService taskService;

    @Override
    public String onSetupTopic() {
        return FileCacheHttpContract.TOPIC_TASK_LIST;
    }

    @Override
    protected void onHandle(List<CacheTask> tasks) {
        taskService.saveTasks(tasks);
    }

    @Override
    protected Type onSetupObjType() {
        return TYPE;
    }
}
