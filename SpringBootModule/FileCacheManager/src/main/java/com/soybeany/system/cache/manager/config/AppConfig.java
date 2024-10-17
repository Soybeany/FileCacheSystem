package com.soybeany.system.cache.manager.config;

import com.soybeany.system.cache.core.security.model.MapRepositoryImpl;
import com.soybeany.system.cache.core.security.model.SecretKeyProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Soybeany
 * @date 2020/12/29
 */
@Configuration
class AppConfig {

    @ConditionalOnMissingBean
    @Bean
    SecretKeyProvider.Repository defaultSecretKeyRepository() {
        return new MapRepositoryImpl();
    }

}
