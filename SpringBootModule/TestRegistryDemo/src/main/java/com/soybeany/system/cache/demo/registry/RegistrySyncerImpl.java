package com.soybeany.system.cache.demo.registry;

import com.soybeany.rpc.registry.BaseRpcRegistrySyncerImpl;
import com.soybeany.rpc.registry.api.IRpcStorageManager;
import com.soybeany.rpc.registry.impl.RpcStorageManagerMemImpl;
import org.springframework.stereotype.Component;

/**
 * @author Soybeany
 * @date 2021/10/28
 */
@Component
public class RegistrySyncerImpl extends BaseRpcRegistrySyncerImpl {

    private RpcStorageManagerMemImpl rpcStorageManager;

    @Override
    protected IRpcStorageManager onSetupStorageManager() {
        return rpcStorageManager = new RpcStorageManagerMemImpl();
    }

    @Override
    protected void onStart() {
        super.onStart();
        rpcStorageManager.startAutoClean(7);
    }

    @Override
    protected void onStop() {
        rpcStorageManager.shutdownAutoClean();
        super.onStop();
    }

}
