package com.soybeany.system.cache.app.service;

import com.soybeany.system.cache.core.model.CacheTask;

import java.util.List;

/**
 * @author Soybeany
 * @date 2022/2/19
 */
public interface TaskService {

    String getToken(String fileToken) throws Exception;

    void postTasks(List<String> fileTokens, TaskConfigurer configurer);

    interface TaskConfigurer {
        void onConfigure(CacheTask task);
    }

}
