package com.soybeany.system.cache.demo.manager.sync;

import com.soybeany.rpc.provider.BaseRpcProviderRegistrySyncerImpl;
import com.soybeany.sync.client.picker.DataPicker;
import com.soybeany.sync.client.picker.DataPickerSimpleImpl;
import com.soybeany.system.cache.core.api.FileCacheContract;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
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
    protected String onSetupInvokeUrl(String ip) {
        return getUrl(false, ip, 8182, "", "/sync/rpc", "");
    }

    @Override
    protected void onSetupImplPkgToScan(Set<String> set) {
        set.add(FileCacheContract.PKG_PATH_TO_SCAN);
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
