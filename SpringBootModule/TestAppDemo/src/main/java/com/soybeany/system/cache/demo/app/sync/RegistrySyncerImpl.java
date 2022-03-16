package com.soybeany.system.cache.demo.app.sync;

import com.soybeany.rpc.core.model.RpcServerInfo;
import com.soybeany.rpc.unit.BaseRpcUnitRegistrySyncerImpl;
import com.soybeany.sync.client.picker.DataPicker;
import com.soybeany.sync.client.picker.DataPickerSimpleImpl;
import com.soybeany.system.cache.demo.app.config.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * @author Soybeany
 * @date 2022/2/23
 */
@Component
public class RegistrySyncerImpl extends BaseRpcUnitRegistrySyncerImpl {

    @Autowired
    private AppConfig appConfig;

    @Override
    public String onSetupGroup() {
        return appConfig.getGroup();
    }

    @Override
    public DataPicker<RpcServerInfo> onGetNewServerPicker(String serviceId) {
        return new DataPickerSimpleImpl<>();
    }

    @Override
    public void onSetupApiPkgToScan(Set<String> paths) {
    }

    @Override
    public void onSetupImplPkgToScan(Set<String> paths) {
        paths.add("com.soybeany.system.cache.demo.app.impl");
    }

    @Override
    public String onSetupInvokeUrl(String ip) {
        return getUrl(false, ip, appConfig.getPort(), appConfig.getContext(), "/api/rpc", "");
    }

    @Override
    public DataPicker<String> onSetupSyncServerPicker() {
        return new DataPickerSimpleImpl<>(appConfig.getRegistryUrls());
    }

    @Override
    protected int onSetupSyncIntervalSec() {
        return appConfig.getRegistrySyncInterval();
    }

}
