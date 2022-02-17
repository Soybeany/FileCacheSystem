package com.soybeany.system.cache.core.interfaces;

import com.soybeany.rpc.core.anno.BdRpc;
import com.soybeany.rpc.core.anno.BdRpcCache;
import com.soybeany.system.cache.core.token.SecretKeyHolder;

/**
 * @author Soybeany
 * @date 2022/1/25
 */
@BdRpc(serviceId = "SecretKeyHolderProvider")
public interface ISecretKeyHolderProvider {

    @BdRpcCache(desc = "密钥管理器", ttl = 10)
    SecretKeyHolder.WithTtl getHolder() throws Exception;

}
