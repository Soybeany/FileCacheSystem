package com.soybeany.system.cache.app;

import com.soybeany.rpc.provider.BaseRpcProviderRegistrySyncerImpl;
import com.soybeany.system.cache.core.api.ICacheAppInfoProvider;
import com.soybeany.system.cache.core.model.CacheAppInfo;

/**
 * @author Soybeany
 * @date 2022/2/18
 */
public abstract class BaseRegistrySyncerImpl extends BaseRpcProviderRegistrySyncerImpl implements ICacheAppInfoProvider {

    @Override
    public CacheAppInfo getInfo() {
        return null;
    }

}
