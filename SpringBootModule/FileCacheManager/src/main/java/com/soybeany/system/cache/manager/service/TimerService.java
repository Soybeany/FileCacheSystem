package com.soybeany.system.cache.manager.service;

import com.soybeany.system.cache.core.util.TimerUtils;
import com.soybeany.system.cache.manager.config.UserConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;

/**
 * 将任务同步至缓存服务器
 *
 * @author Soybeany
 * @date 2020/12/18
 */
@Service
public class TimerService {

    private static final Logger LOG = LoggerFactory.getLogger(TimerService.class);
    private static final int DAY_INTERVAL_SEC = 24 * 60 * 60;

    @Autowired
    private UserConfig userConfig;

    @Autowired
    private TaskService taskService;

    private ExecutorService syncTasksTimer;
    private ExecutorService cleanTasksTimer;

    @PostConstruct
    public void onInit() {
        syncTasksTimer = TimerUtils.getTimer(userConfig.taskSyncIntervalSec, () -> taskService.onSyncTasks(), e -> LOG.warn("syncTasks遇到异常:" + e.getMessage()));
        cleanTasksTimer = TimerUtils.getTimer(DAY_INTERVAL_SEC, () -> taskService.onCleanTasks(), e -> LOG.warn("cleanTasks遇到异常:" + e.getMessage()));
    }

    @PreDestroy
    public void onDestroy() {
        syncTasksTimer.shutdown();
        cleanTasksTimer.shutdown();
    }
}
