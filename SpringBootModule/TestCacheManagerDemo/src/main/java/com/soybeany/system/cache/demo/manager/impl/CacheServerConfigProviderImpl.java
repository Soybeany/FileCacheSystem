package com.soybeany.system.cache.demo.manager.impl;

import com.soybeany.system.cache.core.api.ICacheServerConfigProvider;
import com.soybeany.system.cache.core.model.CacheServerConfig;
import org.springframework.stereotype.Service;

/**
 * @author Soybeany
 * @date 2022/2/18
 */
@Service
public class CacheServerConfigProviderImpl implements ICacheServerConfigProvider {

    @Override
    public CacheServerConfig getConfig() {
        return new CacheServerConfig(
                "D:\\cache-test\\cache",
                "D:\\cache-test\\temp",
                300,
                10,
                5,
                10,
                10,
                3
        );
    }

}
