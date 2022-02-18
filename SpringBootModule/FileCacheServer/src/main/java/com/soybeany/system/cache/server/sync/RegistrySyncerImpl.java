package com.soybeany.system.cache.server.sync;

import com.soybeany.mq.client.plugin.MqClientRegistryPlugin;
import com.soybeany.rpc.consumer.BaseRpcConsumerRegistrySyncerImpl;
import com.soybeany.rpc.core.model.RpcServerInfo;
import com.soybeany.sync.client.api.IClientPlugin;
import com.soybeany.sync.client.picker.DataPicker;
import com.soybeany.sync.client.picker.DataPickerSimpleImpl;
import com.soybeany.system.cache.core.api.FileCacheHttpContract;
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
public class RegistrySyncerImpl extends BaseRpcConsumerRegistrySyncerImpl {

    @Autowired
    private AppConfig appConfig;

    @Override
    protected void onSetupPlugins(List<IClientPlugin<?, ?>> plugins) {
        super.onSetupPlugins(plugins);
        plugins.add(new MqClientRegistryPlugin());
    }

    @Override
    protected DataPicker<RpcServerInfo> onGetNewServerPicker(String serviceId) {
        return new DataPickerSimpleImpl<>();
    }

    @Override
    public void onSetupPkgPathToScan(Set<String> set) {
        set.add(FileCacheHttpContract.PKG_PATH_TO_SCAN);
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
