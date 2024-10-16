package com.soybeany.system.cache.server.config;

import com.soybeany.system.cache.core.security.interfaces.HostProvider;
import com.soybeany.system.cache.core.security.model.PollingHostProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Soybeany
 * @date 2020/11/30
 */
@Component
public class AppConfig {

    @Value("${config.app-servers}")
    public List<ServerInfo> appServers;
    @Value("${config.authorization}")
    public String authorization;
    @Value("${config.file-cache-dir}")
    public String fileCacheDir;
    @Value("${config.max-used-percent}")
    public Float maxUsedPercent;
    @Value("${config.download-timeout-sec}")
    public int downloadTimeoutSec;
    @Value("${config.task-exe-interval-sec}")
    public int taskExeIntervalSec;
    @Value("${config.task-concurrent-max-count}")
    public int taskConcurrentMaxCount;
    @Value("${config.manager-hosts}")
    private String managerHosts;

    public HostProvider getManagerHosts() {
        return PollingHostProvider.fromString(managerHosts);
    }
}
