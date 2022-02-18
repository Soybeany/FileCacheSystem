package com.soybeany.system.cache.demo.registry;

import com.soybeany.rpc.registry.BaseRpcRegistrySyncerImpl;
import com.soybeany.rpc.registry.api.IRpcStorageManager;
import com.soybeany.rpc.registry.impl.RpcStorageManagerMemImpl;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

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

    @PostConstruct
    private void onInit() {
        start();
        rpcStorageManager.startAutoClean(7);
    }

    @PreDestroy
    private void onDestroy() {
        rpcStorageManager.shutdownAutoClean();
        stop();
    }

}
