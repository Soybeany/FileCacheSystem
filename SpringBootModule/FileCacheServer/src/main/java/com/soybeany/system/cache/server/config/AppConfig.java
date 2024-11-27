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

    public String fileCacheDir;
    public Float maxUsedPercent;

    public long tempFileThreshold = 10485760;
    public long tempFileRetainMills = 3600000;
    public int downloadTimeoutSec = 10;
    public int concurrentMaxConnections = 5;
    public int concurrentWaitTimeoutSec = 300;

    public HostProvider managerHosts;

    public void setAppServers(List<ServerInfo> appServers) {
        this.appServers = appServers;
    }

    public void setFileCacheDir(String fileCacheDir) {
        this.fileCacheDir = fileCacheDir;
    }

    public void setMaxUsedPercent(Float maxUsedPercent) {
        this.maxUsedPercent = maxUsedPercent;
    }

    public void setTempFileThresholdM(int tempFileThresholdM) {
        this.tempFileThreshold = tempFileThresholdM * 1024 * 1024L;
    }

    public void setTempFileRetainSec(int tempFileRetainSec) {
        this.tempFileRetainMills = tempFileRetainSec * 1000L;
    }

    public void setDownloadTimeoutSec(int downloadTimeoutSec) {
        this.downloadTimeoutSec = downloadTimeoutSec;
    }

    public void setConcurrentMaxConnections(int concurrentMaxConnections) {
        this.concurrentMaxConnections = concurrentMaxConnections;
    }

    public void setConcurrentWaitTimeoutSec(int concurrentWaitTimeoutSec) {
        this.concurrentWaitTimeoutSec = concurrentWaitTimeoutSec;
    }

    public void setManagerHosts(String managerHosts) {
        this.managerHosts = PollingHostProvider.fromString(managerHosts);
    }

}
