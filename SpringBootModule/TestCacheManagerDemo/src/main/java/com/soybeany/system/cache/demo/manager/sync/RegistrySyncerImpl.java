package com.soybeany.system.cache.demo.manager.sync;

import com.soybeany.rpc.provider.BaseRpcProviderRegistrySyncerImpl;
import com.soybeany.sync.client.picker.DataPicker;
import com.soybeany.sync.client.picker.DataPickerSimpleImpl;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * @author Soybeany
 * @date 2022/1/22
 */
@Component
public class RegistrySyncerImpl extends BaseRpcProviderRegistrySyncerImpl {

    @Override
    public DataPicker<String> onSetupSyncServerPicker() {
        return new DataPickerSimpleImpl<>("http://localhost:8180/bd-api/sync");
    }

    @Override
    protected int onSetupSyncIntervalSec() {
        return 3;
    }

    @Override
    public String onSetupInvokeUrl(String ip) {
        return getUrl(false, ip, 8182, "", "/sync/rpc", "");
    }

    @Override
    public void onSetupImplPkgToScan(Set<String> set) {
        set.add("com.soybeany.system.cache.demo.manager.impl");
    }

}
