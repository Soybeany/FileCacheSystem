package com.soybeany.system.cache.demo.app.sync;

import com.soybeany.rpc.core.model.RpcServerInfo;
import com.soybeany.sync.client.picker.DataPicker;
import com.soybeany.sync.client.picker.DataPickerSimpleImpl;
import com.soybeany.system.cache.app.BaseRegistrySyncerImpl;
import org.springframework.stereotype.Component;

/**
 * @author Soybeany
 * @date 2022/2/23
 */
@Component
public class RegistrySyncerImpl extends BaseRegistrySyncerImpl {

    @Override
    protected String onSetupGroup() {
        return "efb";
    }

    @Override
    protected DataPicker<RpcServerInfo> onGetNewServerPicker(String serviceId) {
        return new DataPickerSimpleImpl<>();
    }

    @Override
    protected String onSetupInvokeUrl(String ip) {
        return "http://localhost:8183/api/rpc";
    }


    @Override
    public DataPicker<String> onSetupSyncServerPicker() {
        return new DataPickerSimpleImpl<>("http://localhost:8180/bd-api/sync");
    }

    @Override
    protected int onSetupSyncIntervalSec() {
        return 5;
    }

}
