package com.soybeany.system.cache.manager.config;

import com.soybeany.rpc.provider.BaseRpcProviderRegistrySyncerImpl;
import com.soybeany.system.cache.manager.key.MapRepositoryImpl;
import com.soybeany.system.cache.manager.service.ISecretKeyRepository;
import com.soybeany.system.cache.manager.sync.BaseCacheManagerSyncerImpl;
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
        return new MapRepositoryImpl();
    }

    @ConditionalOnMissingBean
    @Bean
    BaseRpcProviderRegistrySyncerImpl defaultRpcRegistrySyncer() {
        throw new RuntimeException("请实现BaseRpcProviderRegistrySyncerImpl");
    }

    @ConditionalOnMissingBean
    @Bean
    BaseCacheManagerSyncerImpl defaultMqBrokerSyncer() {
        throw new RuntimeException("请实现BaseMqBrokerSyncerImpl");
    }

}
