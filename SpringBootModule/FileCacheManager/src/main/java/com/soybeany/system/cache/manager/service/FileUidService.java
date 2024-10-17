package com.soybeany.system.cache.manager.service;

import com.soybeany.system.cache.core.dto.FileUid;
import com.soybeany.system.cache.core.security.model.SecretKeyProvider;
import com.soybeany.system.cache.core.token.SecretKeyHolder;
import com.soybeany.system.cache.core.util.TokenUtils;
import com.soybeany.system.cache.manager.config.UserConfig;
import com.soybeany.system.cache.manager.model.CacheLogWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class FileUidService implements IDataProvider {

    @Autowired
    private UserConfig userConfig;
    @Autowired
    private SecretKeyProvider.Repository secretKeyRepository;

    private SecretKeyProvider secretKeyProvider;

    @Override
    public String onGetSecretKeyString() {
        return secretKeyProvider.getHolderString();
    }

    @SuppressWarnings("unused")
    public String toToken(FileUid fileUid) {
        SecretKeyHolder.WithExpiry holder = secretKeyProvider.getHolder();
        return TokenUtils.toToken(holder.map::get, holder.newestKey, fileUid);
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
