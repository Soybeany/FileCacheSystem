package com.soybeany.system.cache.server.service;

import com.soybeany.system.cache.server.config.AppConfig;
import com.soybeany.system.cache.server.config.ServerInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Soybeany
 * @date 2020/12/1
 */
public interface ConfigService {

    /**
     * 获取服务器配置信息
     */
    ServerInfo getServerInfo(String server);

    /**
     * 获取指定服务器的缓存目录
     */
    File getCacheDir(String server);

    /**
     * 获取临时文件目录
     */
    File getTempDir();
}

@Service
class ConfigServiceImpl implements ConfigService {

    @Autowired
    private AppConfig appConfig;

    private final Map<String, ServerInfo> serverInfoMap = new HashMap<>();

    @Override
    public ServerInfo getServerInfo(String server) {
        return Optional.ofNullable(serverInfoMap.get(server)).orElseThrow(() -> new RuntimeException("没有该服务器的相关信息"));
    }

    @Override
    public File getCacheDir(String server) {
        return new File(appConfig.fileCacheDir + server);
    }

    @Override
    public File getTempDir() {
        return new File(appConfig.fileCacheDir + "temp");
    }

    @PostConstruct
    public void init() {
        for (ServerInfo serverInfo : appConfig.appServers) {
            serverInfoMap.put(serverInfo.name, serverInfo);
        }
    }
}
