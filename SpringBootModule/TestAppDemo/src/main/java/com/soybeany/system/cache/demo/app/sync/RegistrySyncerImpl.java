package com.soybeany.system.cache.demo.app.sync;

import com.soybeany.rpc.core.model.RpcServerInfo;
import com.soybeany.sync.client.picker.DataPicker;
import com.soybeany.sync.client.picker.DataPickerSimpleImpl;
import com.soybeany.system.cache.app.sync.BaseRegistrySyncerImpl;
import com.soybeany.system.cache.demo.app.config.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * @author Soybeany
 * @date 2022/2/23
 */
@Component
public class RegistrySyncerImpl extends BaseRegistrySyncerImpl {

    @Autowired
    private AppConfig appConfig;

    @Override
    protected String onSetupGroup() {
        return appConfig.getGroup();
    }

    @Override
    protected DataPicker<RpcServerInfo> onGetNewServerPicker(String serviceId) {
        return new DataPickerSimpleImpl<>();
    }

    @Override
    protected String onSetupInvokeUrl(String ip) {
        return getUrl(false, ip, appConfig.getPort(), appConfig.getContext(), "/api/rpc", "");
    }

    @Override
    protected void onSetupImplPkgToScan(Set<String> set) {
        set.add("com.soybeany.system.cache.demo.app.service");
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
