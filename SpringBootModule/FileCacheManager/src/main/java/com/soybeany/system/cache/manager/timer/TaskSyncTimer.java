package com.soybeany.system.cache.manager.timer;

import com.soybeany.system.cache.core.service.BaseTimerService;
import com.soybeany.system.cache.manager.config.UserConfig;
import com.soybeany.system.cache.manager.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.LocalDateTime;

/**
 * 将任务同步至缓存服务器
 *
 * @author Soybeany
 * @date 2020/12/18
 */
@Component
public class TaskSyncTimer extends BaseTimerService {

    private static final Logger LOG = LoggerFactory.getLogger(TaskSyncTimer.class);

    @Autowired
    private UserConfig userConfig;

    @Autowired
    private TaskService taskService;

    @Override
    protected void onException(Throwable e) {
        LOG.warn("TaskSyncTimer遇到异常:" + e.getMessage());
    }

    @Override
    protected LocalDateTime onSetupFirstTriggerTime() {
        return LocalDateTime.now().plusSeconds(5);
    }

    @Override
    protected long onSetupIntervalMillis() {
        return userConfig.taskSyncIntervalSec * 1000L;
    }

    @Override
    protected void onSignal() {
        taskService.syncTasks();
    }

    @PostConstruct
    private void onInit() {
        init();
    }

    @PreDestroy
    private void onDestroy() {
        destroy();
    }
}
