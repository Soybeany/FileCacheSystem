package com.soybeany.system.cache.server.service;

import com.soybeany.rpc.core.api.IRpcServiceProxy;
import com.soybeany.system.cache.core.interfaces.ICacheServerConfigProvider;
import com.soybeany.system.cache.core.model.CacheServerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * @author Soybeany
 * @date 2022/2/16
 */
@Service
public class ConfigServiceImpl implements ConfigService {

    @Autowired
    private IRpcServiceProxy serviceProxy;

    private ICacheServerConfigProvider provider;

    @Override
    public CacheServerConfig get() {
        return provider.getConfig();
    }

    @PostConstruct
    private void onInit() {
        provider = serviceProxy.get(ICacheServerConfigProvider.class);
    }

}
