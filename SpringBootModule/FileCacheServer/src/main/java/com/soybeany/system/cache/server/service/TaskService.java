package com.soybeany.system.cache.server.service;

import com.soybeany.system.cache.core.dto.CacheTask;
import com.soybeany.system.cache.core.dto.FileUid;
import com.soybeany.system.cache.core.util.TimerUtils;
import com.soybeany.system.cache.server.config.AppConfig;
import com.soybeany.system.cache.server.util.InfoFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author Soybeany
 * @date 2020/12/21
 */
@Service
public class TaskService {

    private static final Logger LOG = LoggerFactory.getLogger(TaskService.class);
    private static final String DIR_TASK = "/task";

    private ExecutorService timer;
    private ExecutorService taskExecutor;
    @Autowired
    private AppConfig appConfig;
    @Autowired
    private CacheInfoService cacheInfoService;

    @SuppressWarnings("AlibabaThreadShouldSetName")
    @PostConstruct
    void onInit() {
        taskExecutor = new ThreadPoolExecutor(0, appConfig.taskConcurrentMaxCount,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>());
        timer = TimerUtils.getTimer(appConfig.taskExeIntervalSec, this::executeTasks);
    }

    @PreDestroy
    void onDestroy() {
        timer.shutdown();
        taskExecutor.shutdown();
    }

    /**
     * 保存指定的任务
     */
    public void saveTasks(List<CacheTask> tasks) {
        if (null == tasks || tasks.isEmpty()) {
            return;
        }
        for (CacheTask task : tasks) {
            FileUid fileUid = FileUid.fromString(task.fileUid);
            InfoFileUtils.write(getTaskFile(fileUid), task);
        }
    }

    @SuppressWarnings("unused")
    public Map<String, Integer> queryTaskStates(List<String> fileUidStrList) {
        Map<String, Integer> result = new HashMap<>();
        for (String fileUidStr : fileUidStrList) {
            FileUid fileUid = FileUid.fromString(fileUidStr);
            // 查看缓存是否存在
            boolean isCacheExist = cacheInfoService.isCacheExist(fileUid);
            if (isCacheExist) {
                result.put(fileUidStr, CacheTask.HAS_CACHE);
                continue;
            }
            // 查看任务是否存在
            result.put(fileUidStr, getTaskFile(fileUid).exists() ? CacheTask.HAS_TASK_NO_CACHE : CacheTask.NO_TASK_NO_CACHE);
        }
        return result;
    }

    /**
     * 执行任务
     */
    public void executeTasks() {
        File[] serverDirs = Optional.ofNullable(new File(appConfig.fileCacheDir).listFiles()).orElse(new File[0]);
        Info info = new Info();
        List<Future<?>> futures = new ArrayList<>();
        for (File serverDir : serverDirs) {
            for (File taskFile : Optional.ofNullable(new File(serverDir, DIR_TASK).listFiles()).orElse(new File[0])) {
                Optional<CacheTask> taskOpt = InfoFileUtils.read(taskFile, CacheTask.class);
                if (taskOpt.isPresent()) {
                    onTaskExist(futures, info, taskOpt.get());
                } else {
                    info.failureCount++;
                    LOG.warn("任务(" + taskFile.getName() + ")解析异常");
                }
                boolean ignore = taskFile.delete();
            }
        }
        // 等待全部任务执行完毕
        futures.forEach(future -> {
            try {
                future.get();
            } catch (Exception ignore) {
            }
        });
        // 日志输出
        info.print();
    }

    private void onTaskExist(List<Future<?>> futures, Info info, CacheTask task) {
        if (!task.canExe(info.curHour)) {
            info.notStartCount++;
            return;
        }
        Future<?> future = taskExecutor.submit(() -> {
            try {
                cacheInfoService.receiveCacheInfo(FileUid.fromString(task.fileUid), (dataInfo, file) -> null);
                info.successCount++;
            } catch (Exception e) {
                info.failureCount++;
                LOG.warn("任务(" + task.fileUid + ")执行失败：" + e.getMessage());
            }
        });
        futures.add(future);
    }

    private File getTaskFile(FileUid fileUid) {
        return new File(appConfig.fileCacheDir + "/" + fileUid.server + DIR_TASK, fileUid.fileId);
    }

    private static class Info {
        final int curHour = LocalDateTime.now().getHour();
        int successCount, failureCount, notStartCount;

        void print() {
            if (successCount != 0 || notStartCount != 0 || failureCount != 0) {
                LOG.info("主动缓存任务，成功:" + successCount + "个，未开始:" + notStartCount + "个，失败:" + failureCount + "个");
            }
        }
    }

}
