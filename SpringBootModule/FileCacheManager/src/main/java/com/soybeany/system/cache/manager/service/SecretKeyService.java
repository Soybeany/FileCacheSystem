package com.soybeany.system.cache.manager.service;

import com.soybeany.system.cache.core.security.model.SecretKeyProvider;
import com.soybeany.system.cache.manager.config.UserConfig;
import com.soybeany.system.cache.manager.model.CacheLogWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class SecretKeyService {

    @Autowired
    private UserConfig userConfig;
    @Autowired
    private SecretKeyProvider.Repository secretKeyRepository;

    private SecretKeyProvider secretKeyProvider;

    public SecretKeyProvider getProvider() {
        return secretKeyProvider;
    }

    @PostConstruct
    public void init() {
        secretKeyProvider = new SecretKeyProvider(
                userConfig.oldKeyCount,
                userConfig.futureKeyCount,
                userConfig.renewFrequencySec,
                secretKeyRepository, new CacheLogWriter()
        );
    }
}
