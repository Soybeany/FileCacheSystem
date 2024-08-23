package com.soybeany.system.cache.server.service;

import com.soybeany.system.cache.core.dto.FileUid;
import com.soybeany.system.cache.core.security.model.SecretKeyRetriever;
import com.soybeany.system.cache.core.util.TokenUtils;
import com.soybeany.system.cache.server.config.AppConfig;
import com.soybeany.system.cache.server.model.CacheLogWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * @author Soybeany
 * @date 2020/12/1
 */
@Service
public class FileUidService {

    @Autowired
    private AppConfig appConfig;

    private SecretKeyRetriever keyRetriever;

    public FileUid toFileUid(String token) {
        return TokenUtils.fromToken(key -> keyRetriever.getHolder().getSecretKey(key), token);
    }

    @PostConstruct
    void init() {
        keyRetriever = new SecretKeyRetriever(appConfig.hostProvider, new CacheLogWriter());
    }

}
