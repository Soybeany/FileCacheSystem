package com.soybeany.system.cache.server.service;

import com.soybeany.system.cache.core.model.CacheTask;

import java.util.List;

/**
 * @author Soybeany
 * @date 2020/12/21
 */
public interface TaskService {

    /**
     * 保存指定的任务
     */
    void saveTasks(List<CacheTask.WithStamp> tasks);

}

