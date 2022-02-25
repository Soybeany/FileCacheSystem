package com.soybeany.system.cache.manager.config;

import com.soybeany.mq.core.api.IMqMsgStorageManager;
import com.soybeany.rpc.provider.BaseRpcProviderRegistrySyncerImpl;
import com.soybeany.system.cache.manager.impl.SecretKeyMapRepositoryImpl;
import com.soybeany.system.cache.manager.service.ISecretKeyRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Soybeany
 * @date 2020/12/29
 */
@Configuration
class BeanConfig {

    @ConditionalOnMissingBean
    @Bean
    ISecretKeyRepository defaultSecretKeyRepository() {
        return new SecretKeyMapRepositoryImpl();
    }

    @ConditionalOnMissingBean
    @Bean
    BaseRpcProviderRegistrySyncerImpl defaultRpcRegistrySyncer() {
        throw new RuntimeException("请实现BaseRpcProviderRegistrySyncerImpl");
    }

    @ConditionalOnMissingBean
    @Bean
    IMqMsgStorageManager defaultMqMsgStorageManager() {
        throw new RuntimeException("请实现IMqMsgStorageManager");
    }

}
