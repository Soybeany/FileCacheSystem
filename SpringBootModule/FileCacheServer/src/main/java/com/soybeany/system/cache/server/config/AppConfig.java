package com.soybeany.system.cache.server.config;

import com.soybeany.system.cache.core.interfaces.HostProvider;
import com.soybeany.system.cache.core.model.PollingHostProvider;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Soybeany
 * @date 2020/11/30
 */
@Component
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.soybeany.system.cache.server.repository")
@EntityScan(basePackages = "com.soybeany.system.cache.server.repository")
@ConfigurationProperties(prefix = "config")
public class AppConfig {

    public List<ServerInfo> appServers;
    public List<TimerInfo> timers;

    public String authorization;
    public String fileCacheDir;
    public long recordExpirySec;
    public long tempFileExpirySec;
    public long contentInfoLockTimeoutSec;
    public int downloadTimeoutSec;
    public int taskConcurrentMaxCount;
    public int taskRetryCount;

    public HostProvider hostProvider;

    public void setAppServers(List<ServerInfo> appServers) {
        this.appServers = appServers;
    }

    public void setTimers(List<TimerInfo> timers) {
        this.timers = timers;
    }

    public void setManagerHosts(String managerHosts) {
        this.hostProvider = PollingHostProvider.fromString(managerHosts);
    }

    public void setAuthorization(String authorization) {
        this.authorization = authorization;
    }

    public void setFileCacheDir(String fileCacheDir) {
        this.fileCacheDir = fileCacheDir;
    }

    public void setRecordExpirySec(long recordExpirySec) {
        this.recordExpirySec = recordExpirySec;
    }

    public void setTempFileExpirySec(long tempFileExpirySec) {
        this.tempFileExpirySec = tempFileExpirySec;
    }

    public void setContentInfoLockTimeoutSec(long contentInfoLockTimeoutSec) {
        this.contentInfoLockTimeoutSec = contentInfoLockTimeoutSec;
    }

    public void setDownloadTimeoutSec(int downloadTimeoutSec) {
        this.downloadTimeoutSec = downloadTimeoutSec;
    }

    public void setTaskConcurrentMaxCount(int taskConcurrentMaxCount) {
        this.taskConcurrentMaxCount = taskConcurrentMaxCount;
    }

    public void setTaskRetryCount(int taskRetryCount) {
        this.taskRetryCount = taskRetryCount;
    }
}
