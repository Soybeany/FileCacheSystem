package com.soybeany.system.cache.core.interfaces;

import com.soybeany.rpc.core.anno.BdRpc;
import com.soybeany.rpc.core.anno.BdRpcCache;
import com.soybeany.system.cache.core.model.CacheAppInfo;

/**
 * @author Soybeany
 * @date 2022/1/27
 */
@BdRpc(serviceId = "CacheAppInfoProvider")
public interface ICacheAppInfoProvider {

    @BdRpcCache(desc = "应用服务器信息", ttl = 30 * 60)
    CacheAppInfo getInfo();

}
