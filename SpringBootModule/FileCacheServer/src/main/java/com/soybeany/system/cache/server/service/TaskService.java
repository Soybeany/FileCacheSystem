package com.soybeany.system.cache.server.service;

import com.soybeany.system.cache.core.model.CacheTask;
import com.soybeany.system.cache.core.model.FileUid;
import com.soybeany.system.cache.server.config.AppConfig;
import com.soybeany.system.cache.server.repository.TaskInfo;
import com.soybeany.system.cache.server.repository.TaskInfoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Soybeany
 * @date 2020/12/21
 */
public interface TaskService {

    /**
     * 保存指定的任务
     */
    void saveTasks(List<CacheTask> tasks);

    /**
     * 查找并执行新的任务
     */
    void findAndExecuteNewTasks();

}

@Service
class TaskServiceImpl implements TaskService {

    private static final Logger LOG = LoggerFactory.getLogger(TaskServiceImpl.class);

    private final Set<String> taskTags = new HashSet<>();
    private ExecutorService taskExecutor;
    @Autowired
    private AppConfig appConfig;
    @Autowired
    private CacheInfoService cacheInfoService;
    @Autowired
    private TaskInfoRepository taskInfoRepository;

    @SuppressWarnings("AlibabaThreadShouldSetName")
    @PostConstruct
    void onInit() {
        taskExecutor = new ThreadPoolExecutor(0, appConfig.taskConcurrentMaxCount,
                60L, TimeUnit.SECONDS,
                new SynchronousQueue<>(true));
    }

    @PreDestroy
    void onDestroy() {
        taskExecutor.shutdown();
    }

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
    public void findAndExecuteNewTasks() {
        // 获取全部待执行的任务
        List<TaskInfo> tasks = taskInfoRepository.findByPriorityGreaterThanOrderByPriorityDesc(TaskInfo.PRIORITY_FINISH);
        // 将新的任务添加到执行队列
        int newAdded = 0;
        for (TaskInfo taskInfo : tasks) {
            if (taskTags.add(taskInfo.fileUid)) {
                taskExecutor.submit(() -> executeTask(taskInfo.fileUid));
                newAdded++;
            }
        }
        // 日志输出
        LOG.info("主动缓存任务，待执行:" + taskTags.size() + "个，新增:" + newAdded + "个");
    }

    private TaskInfo toTaskInfo(CacheTask task) {
        TaskInfo info = taskInfoRepository.findByFileUid(task.fileUid);
        if (null == info) {
            info = new TaskInfo();
        }
        info.fileUid = task.fileUid;
        info.priority = appConfig.taskRetryCount;
        info.canExeFrom = task.getCanExeFrom();
        info.canExeTo = task.getCanExeTo();
        return info;
    }

    private void executeTask(String fileUid) {
        boolean success = true;
        try {
            if (!isTaskShouldExecute(fileUid)) {
                LOG.info("“" + fileUid + "”未到可执行时间，暂不执行");
                return;
            }
            cacheInfoService.ensureFileAndGetFileInfo(FileUid.fromString(fileUid));
            LOG.info("“" + fileUid + "”执行成功");
        } catch (Exception e) {
            LOG.warn("“" + fileUid + "”执行异常:" + e.getMessage());
            success = false;
        } finally {
            synchronized (taskTags) {
                taskTags.remove(fileUid);
            }
        }
        editTaskInfo(success, fileUid);
    }

    private boolean isTaskShouldExecute(String fileUid) {
        TaskInfo taskInfo = taskInfoRepository.findByFileUid(fileUid);
        int curHour = LocalDateTime.now().getHour();
        return curHour >= taskInfo.canExeFrom && curHour <= taskInfo.canExeTo;
    }

    private void editTaskInfo(boolean success, String fileUid) {
        TaskInfo taskInfo = taskInfoRepository.findByFileUid(fileUid);
        if (null == taskInfo) {
            return;
        }
        if (success) {
            taskInfo.priority = TaskInfo.PRIORITY_FINISH;
        } else {
            taskInfo.priority--;
        }
        taskInfoRepository.save(taskInfo);
    }

}
