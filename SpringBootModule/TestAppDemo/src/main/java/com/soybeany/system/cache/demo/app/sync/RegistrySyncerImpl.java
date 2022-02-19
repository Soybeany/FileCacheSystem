package com.soybeany.system.cache.demo.app.sync;

import com.soybeany.mq.client.plugin.MqClientRegistryPlugin;
import com.soybeany.rpc.consumer.BaseRpcConsumerRegistrySyncerImpl;
import com.soybeany.rpc.core.model.RpcServerInfo;
import com.soybeany.rpc.provider.api.IRpcServiceExecutor;
import com.soybeany.rpc.provider.plugin.RpcProviderPlugin;
import com.soybeany.sync.client.api.IClientPlugin;
import com.soybeany.sync.client.picker.DataPicker;
import com.soybeany.sync.client.picker.DataPickerSimpleImpl;
import com.soybeany.sync.core.model.SyncDTO;
import com.soybeany.system.cache.core.api.FileCacheContract;
import com.soybeany.system.cache.demo.app.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Soybeany
 * @date 2021/10/28
 */
@Slf4j
@Component
public class RegistrySyncerImpl extends BaseRpcConsumerRegistrySyncerImpl implements IRpcServiceExecutor {

    private RpcProviderPlugin providerPlugin;

    @Override
    protected void onSetupPlugins(List<IClientPlugin<?, ?>> plugins) {
        super.onSetupPlugins(plugins);
        plugins.add(new MqClientRegistryPlugin());
        plugins.add(providerPlugin = new RpcProviderPlugin(
                onSetupSystem(),
                onSetupVersion(),
                TaskService.GROUP,
                appContext,
                "http://localhost:8183/api/rpc",
                new HashSet<>(Collections.singletonList(FileCacheContract.PKG_PATH_TO_SCAN))
        ));
    }

    @Override
    public SyncDTO execute(HttpServletRequest request, HttpServletResponse response) {
        return providerPlugin.execute(request, response);
    }

    @Override
    protected DataPicker<RpcServerInfo> onGetNewServerPicker(String serviceId) {
        return new DataPickerSimpleImpl<>();
    }

    @Override
    public void onSetupPkgPathToScan(Set<String> set) {
        set.add(FileCacheContract.PKG_PATH_TO_SCAN);
    }

    @Override
    public DataPicker<String> onSetupSyncServerPicker() {
        return new DataPickerSimpleImpl<>("http://localhost:8180/bd-api/sync");
    }

    @Override
    public int onSetupSyncIntervalInSec() {
        return 5;
    }

    @PostConstruct
    private void onInit() {
        start();
    }

    @PreDestroy
    private void onDestroy() {
        stop();
    }

}
