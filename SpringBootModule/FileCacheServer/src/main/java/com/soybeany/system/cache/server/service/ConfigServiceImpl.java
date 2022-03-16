package com.soybeany.system.cache.server.service;

import com.soybeany.rpc.consumer.anno.BdRpcWired;
import com.soybeany.system.cache.core.api.ICacheServerConfigProvider;
import com.soybeany.system.cache.core.model.CacheServerConfig;
import org.springframework.stereotype.Service;

/**
 * @author Soybeany
 * @date 2022/2/16
 */
@Service
public class ConfigServiceImpl implements ConfigService {

    @BdRpcWired
    private ICacheServerConfigProvider provider;

    @Override
    public CacheServerConfig get() {
        return provider.getConfig();
    }

}
