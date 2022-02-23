package com.soybeany.system.cache.server.service;

import com.soybeany.system.cache.core.model.CacheTask;
import com.soybeany.system.cache.core.model.FileUid;
import com.soybeany.system.cache.core.service.BaseTimerService;
import com.soybeany.system.cache.server.model.TaskInfoP;
import com.soybeany.system.cache.server.repository.DbDAO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Soybeany
 * @date 2022/2/17
 */
@Slf4j
@Service
public class TaskServiceImpl extends BaseTimerService implements TaskService {

    @SuppressWarnings("AlibabaThreadShouldSetName")
    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(0, 5,
            60L, TimeUnit.SECONDS,
            new SynchronousQueue<>());

    @Autowired
    private ConfigService configService;
    @Autowired
    private FileManagerService fileManagerService;
    @Autowired
    private DbDAO dbDAO;

    private final Set<String> executingTasks = new HashSet<>();

    @Override
    public synchronized void saveTasks(List<CacheTask.WithStamp> tasks) {
        if (null == tasks || tasks.isEmpty()) {
            return;
        }
        List<TaskInfoP> list = new ArrayList<>();
        for (CacheTask.WithStamp task : tasks) {
            list.add(toTaskInfo(task));
        }
        dbDAO.saveAllTaskInfo(list);
    }

    @Override
    protected void onSignal() {
        // 设置最新的并发数配置
        setupPoolSize(configService.get().getTaskConcurrentMaxCount());
        // 查询需要执行的任务
        List<TaskInfoP> tasks = dbDAO.findTaskInfoTasksToExecute(LocalDateTime.now().getHour());
        // 将新的任务添加到执行队列
        int newAdded = 0;
        for (TaskInfoP taskInfoP : tasks) {
            String fileUid = taskInfoP.fileUid;
            boolean shouldExecute;
            synchronized (executingTasks) {
                shouldExecute = executingTasks.add(fileUid);
            }
            if (shouldExecute) {
                executor.submit(() -> executeTask(fileUid));
                newAdded++;
            }
        }
        // 日志输出
        log.info("主动缓存任务，待执行:" + executingTasks.size() + "个，新增:" + newAdded + "个");
    }

    @Override
    protected int onSetupIntervalSec() {
        return configService.get().getTaskExeIntervalSec();
    }

    // ***********************内部方法****************************

    private void setupPoolSize(int size) {
        executor.setCorePoolSize(size);
        executor.setMaximumPoolSize(size);
    }

    private TaskInfoP toTaskInfo(CacheTask.WithStamp task) {
        TaskInfoP info = dbDAO.findTaskInfoByFileUid(task.getFileUid());
        if (null == info) {
            info = new TaskInfoP();
        }
        info.fileUid = task.getFileUid();
        info.priority = configService.get().getTaskRetryCount();
        info.stamp = task.getStamp();
        info.canExeFrom = task.getCanExeFrom();
        info.canExeTo = task.getCanExeTo();
        return info;
    }

    private void executeTask(String fileUid) {
        boolean success = true;
        try {
            fileManagerService.getFile(FileUid.fromString(fileUid), 60 * 60);
            log.info("“" + fileUid + "”执行成功");
        } catch (Exception e) {
            log.warn("“" + fileUid + "”执行异常:" + e.getMessage());
            success = false;
        } finally {
            synchronized (executingTasks) {
                executingTasks.remove(fileUid);
            }
        }
        updateTaskInfo(success, fileUid);
    }

    private void updateTaskInfo(boolean success, String fileUid) {
        TaskInfoP taskInfoP = dbDAO.findTaskInfoByFileUid(fileUid);
        if (null == taskInfoP) {
            return;
        }
        if (success) {
            taskInfoP.priority = TaskInfoP.PRIORITY_FINISH;
        } else {
            taskInfoP.priority--;
        }
        dbDAO.saveTaskInfo(taskInfoP);
    }

    @PostConstruct
    private void onInit() {
        init();
    }

    @PreDestroy
    private void onDestroy() {
        destroy();
        executor.shutdown();
    }

}
