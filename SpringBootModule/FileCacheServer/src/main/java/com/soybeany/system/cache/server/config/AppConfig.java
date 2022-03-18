package com.soybeany.system.cache.server.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;

/**
 * @author Soybeany
 * @date 2020/11/30
 */
@Setter
@Getter
@Component
@EnableJpaAuditing
@EnableJpaRepositories("com.soybeany.system.cache.server.repository")
@EntityScan("com.soybeany.system.cache.server.model")
@ComponentScan("com.soybeany.system.cache.server")
@ConfigurationProperties(prefix = "config")
public class AppConfig {

    private String[] registryUrls;
    private Integer registrySyncInterval;
    private Integer taskSyncInterval;

    public void setRegistryUrls(String urls) {
        registryUrls = urls.split("\\s*,\\s*");
    }

}
