package com.soybeany.system.cache.server.config;

import com.soybeany.system.cache.core.security.interfaces.HostProvider;
import com.soybeany.system.cache.core.security.model.PollingHostProvider;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Soybeany
 * @date 2020/11/30
 */
@Component
@ConfigurationProperties(prefix = "config")
public class AppConfig {

    public List<ServerInfo> appServers;

    public String authorization;
    public String fileCacheDir;

    public int downloadTimeoutSec;

    public int taskExeIntervalSec;
    public int taskConcurrentMaxCount;

    public HostProvider hostProvider;

    public void setAppServers(List<ServerInfo> appServers) {
        this.appServers = appServers;
    }

    public void setAuthorization(String authorization) {
        this.authorization = authorization;
    }

    public void setFileCacheDir(String fileCacheDir) {
        this.fileCacheDir = fileCacheDir;
    }

    public void setDownloadTimeoutSec(int downloadTimeoutSec) {
        this.downloadTimeoutSec = downloadTimeoutSec;
    }

    public void setTaskExeIntervalSec(int taskExeIntervalSec) {
        this.taskExeIntervalSec = taskExeIntervalSec;
    }

    public void setTaskConcurrentMaxCount(int taskConcurrentMaxCount) {
        this.taskConcurrentMaxCount = taskConcurrentMaxCount;
    }

    public void setManagerHosts(String managerHosts) {
        this.hostProvider = PollingHostProvider.fromString(managerHosts);
    }

}
