package com.soybeany.system.cache.manager.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Soybeany
 * @date 2020/12/10
 */
@Component
@ConfigurationProperties(prefix = "user-config")
public class UserConfig {

    public int oldKeyCount;
    public int futureKeyCount;
    public int renewFrequencySec;
    public int taskSyncIntervalSec;
    public int taskSyncMaxDay;

    public void setOldKeyCount(int oldKeyCount) {
        this.oldKeyCount = oldKeyCount;
    }

    public void setFutureKeyCount(int futureKeyCount) {
        this.futureKeyCount = futureKeyCount;
    }

    public void setRenewFrequencySec(int renewFrequencySec) {
        this.renewFrequencySec = renewFrequencySec;
    }

    public void setTaskSyncIntervalSec(int taskSyncIntervalSec) {
        this.taskSyncIntervalSec = taskSyncIntervalSec;
    }

    public void setTaskSyncMaxDay(int taskSyncMaxDay) {
        this.taskSyncMaxDay = taskSyncMaxDay;
    }
}
