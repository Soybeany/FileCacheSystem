package com.soybeany.system.cache.manager.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Soybeany
 * @date 2020/12/10
 */
@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "cache-manager-config")
public class UserConfig {

    private int renewFrequencySec;
    private int taskSyncMaxDay;

}
