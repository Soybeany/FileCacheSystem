package com.soybeany.system.cache.manager.service;

import com.soybeany.system.cache.core.model.CacheTask;

import java.util.List;

/**
 * @author Soybeany
 * @date 2020/12/23
 */
public interface TaskService {

    void saveTasks(List<CacheTask> tasks);

    void syncTasks();

}
