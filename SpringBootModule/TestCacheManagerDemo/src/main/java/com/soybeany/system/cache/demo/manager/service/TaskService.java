package com.soybeany.system.cache.demo.manager.service;

import com.google.gson.Gson;
import com.soybeany.system.cache.core.interfaces.FileCacheHttpContract;
import com.soybeany.system.cache.core.model.CacheTask;
import com.soybeany.system.cache.core.util.CacheCoreTimeUtils;
import com.soybeany.system.cache.demo.manager.repository.CacheServerInfo;
import com.soybeany.system.cache.demo.manager.repository.CacheServerInfoRepository;
import com.soybeany.system.cache.demo.manager.repository.TaskInfo;
import com.soybeany.system.cache.demo.manager.repository.TaskInfoRepository;
import com.soybeany.system.cache.manager.config.UserConfig;
import com.soybeany.system.cache.manager.service.TaskService;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Soybeany
 * @date 2020/12/23
 */
@Primary
@Service
class TaskServiceImpl extends BaseService implements TaskService, FileCacheHttpContract {

    private static final Logger LOG = LoggerFactory.getLogger(TaskServiceImpl.class);
    private static final MediaType JSON_TYPE = MediaType.parse("application/json;charset=utf-8");
    private static final Gson GSON = new Gson();

    @Autowired
    private UserConfig userConfig;
    @Autowired
    private CacheServerInfoRepository cacheServerInfoRepository;
    @Autowired
    private TaskInfoRepository taskInfoRepository;

    @Override
    public void saveTasks(List<CacheTask> tasks) {
        if (null == tasks || tasks.isEmpty()) {
            return;
        }
        List<TaskInfo> list = new LinkedList<>();
        for (CacheTask task : tasks) {
            list.add(toTaskInfo(task));
        }
        taskInfoRepository.saveAll(list);
    }

    @Override
    public void syncTasks() {
        execute("任务同步", this::postTasks, (info, count) -> {
            String msg = "“" + info.desc + "”";
            msg += (count > 0 ? "同步“" + count + "”条数据成功" : "没有待同步的数据");
            LOG.info(msg);
        });
    }

    private Integer postTasks(CacheServerInfo info) throws IOException {
        // 获取待同步的任务
        List<TaskInfo> tasks = getTasks(info.syncedTimestamp);
        if (null == tasks) {
            tasks = Collections.emptyList();
        }
        // 同步任务
        List<CacheTask> cacheTasks = toCacheTasks(tasks);
        Request request = new Request.Builder()
                .url(info.host + FileCacheHttpContract.POST_TASK_LIST)
                .header(FileCacheHttpContract.AUTHORIZATION, info.authorization)
                .post(RequestBody.create(JSON_TYPE, GSON.toJson(cacheTasks)))
                .build();
        getResponse(request);
        // 更新信息
        if (!tasks.isEmpty()) {
            info.syncedTimestamp = tasks.get(0).getLastModifyTime();
        }
        info.lastSyncTime = new Date();
        cacheServerInfoRepository.save(info);
        return tasks.size();
    }

    @Nullable
    private List<TaskInfo> getTasks(Date timestamp) {
        LocalDateTime now = LocalDateTime.now();
        if (null == timestamp) {
            timestamp = CacheCoreTimeUtils.toDate(now.plusDays(-userConfig.taskSyncMaxDay));
        }
        if (!needExecute(timestamp, userConfig.taskSyncIntervalSec)) {
            return null;
        }
        Date curDate = CacheCoreTimeUtils.toDate(now);
        return taskInfoRepository.findByLastModifyTimeAfterAndLastModifyTimeBeforeOrderByLastModifyTimeDesc(timestamp, curDate);
    }

    private List<CacheTask> toCacheTasks(List<TaskInfo> list) {
        List<CacheTask> tasks = new LinkedList<>();
        for (TaskInfo info : list) {
            tasks.add(toCacheTask(info));
        }
        return tasks;
    }

    private TaskInfo toTaskInfo(CacheTask task) {
        TaskInfo info = taskInfoRepository.findByFileUid(task.fileUid);
        if (null == info) {
            info = new TaskInfo();
        }
        info.fileUid = task.fileUid;
        info.canExeFrom = task.getCanExeFrom();
        info.canExeTo = task.getCanExeTo();
        return info;
    }

    private CacheTask toCacheTask(TaskInfo info) {
        CacheTask task = new CacheTask();
        task.fileUid = info.fileUid;
        task.setCanExeFrom(info.canExeFrom);
        task.setCanExeTo(info.canExeTo);
        return task;
    }
}
