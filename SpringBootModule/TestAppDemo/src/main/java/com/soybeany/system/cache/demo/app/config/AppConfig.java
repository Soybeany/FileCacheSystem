package com.soybeany.system.cache.demo.app.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Soybeany
 * @date 2020/11/30
 */
@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "config")
public class AppConfig {

    private String[] registryUrls;
    private Integer registrySyncInterval;
    private Integer port;
    private String context;
    private String group;

    public void setRegistryUrls(String urls) {
        registryUrls = urls.split("\\s*,\\s*");
    }

}
