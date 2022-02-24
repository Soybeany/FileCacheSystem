package com.soybeany.system.cache.core.api;

import com.soybeany.rpc.core.anno.BdRpc;
import com.soybeany.rpc.core.anno.BdRpcCache;
import com.soybeany.rpc.core.anno.BdRpcSerialize;
import com.soybeany.system.cache.core.model.SecretKeyHolder;

/**
 * @author Soybeany
 * @date 2022/1/25
 */
@BdRpc
public interface ISecretKeyHolderProvider {

    @BdRpcSerialize
    @BdRpcCache(desc = "密钥管理器", ttl = 60, needLog = false)
    SecretKeyHolder getHolder() throws Exception;

}
