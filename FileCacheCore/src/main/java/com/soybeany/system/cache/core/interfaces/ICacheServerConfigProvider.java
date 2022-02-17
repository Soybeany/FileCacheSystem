package com.soybeany.system.cache.core.interfaces;

import com.soybeany.rpc.core.anno.BdRpc;
import com.soybeany.rpc.core.anno.BdRpcCache;
import com.soybeany.system.cache.core.model.CacheServerConfig;

/**
 * @author Soybeany
 * @date 2022/2/16
 */
@BdRpc(serviceId = "CacheServerConfigProvider")
public interface ICacheServerConfigProvider {

    @BdRpcCache(desc = "缓存服务器配置", ttl = 3600, enableRenewExpiredCache = true)
    CacheServerConfig getConfig();

}
