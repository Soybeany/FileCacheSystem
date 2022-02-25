package com.soybeany.system.cache.demo.manager.impl;

import com.soybeany.system.cache.manager.config.UserConfig;
import com.soybeany.system.cache.manager.service.BaseSecretKeyHolderProvider;
import com.soybeany.system.cache.manager.service.ISecretKeyRepository;
import org.springframework.stereotype.Component;

/**
 * @author Soybeany
 * @date 2022/2/25
 */
@Component
public class SecretKeyHolderProviderImpl extends BaseSecretKeyHolderProvider {
    public SecretKeyHolderProviderImpl(UserConfig userConfig, ISecretKeyRepository mRepository) {
        super(userConfig, mRepository);
    }
}
