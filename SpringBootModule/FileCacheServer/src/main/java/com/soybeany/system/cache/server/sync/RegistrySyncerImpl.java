package com.soybeany.system.cache.server.sync;

import com.soybeany.mq.core.plugin.MqRegistryPlugin;
import com.soybeany.rpc.consumer.BaseRpcRegistrySyncerImpl;
import com.soybeany.rpc.core.model.RpcServerInfo;
import com.soybeany.sync.core.api.IClientPlugin;
import com.soybeany.sync.core.picker.DataPicker;
import com.soybeany.sync.core.picker.DataPickerSimpleImpl;
import com.soybeany.system.cache.server.config.AppConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author Soybeany
 * @date 2021/10/28
 */
@Slf4j
@Component
public class RegistrySyncerImpl extends BaseRpcRegistrySyncerImpl {

    @Autowired
    private AppConfig appConfig;

    @Override
    protected void onSetupPlugins(List<IClientPlugin<?, ?>> plugins) {
        super.onSetupPlugins(plugins);
        plugins.add(new MqRegistryPlugin());
    }

    @Override
    protected DataPicker<RpcServerInfo> onGetNewServerPicker(String serviceId) {
        return new DataPickerSimpleImpl<>();
    }

    @Override
    public void onSetupPkgPathToScan(Set<String> set) {
        set.add("com.soybeany.system.cache");
    }

    @Override
    public DataPicker<String> onSetupSyncServerPicker() {
        return new DataPickerSimpleImpl<>(appConfig.getRegistryUrls());
    }

    @Override
    public int onSetupSyncIntervalInSec() {
        return Optional.ofNullable(appConfig.getRegistrySyncInterval()).orElse(30);
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
