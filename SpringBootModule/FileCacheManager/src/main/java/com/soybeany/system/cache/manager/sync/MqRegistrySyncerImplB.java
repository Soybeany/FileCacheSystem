package com.soybeany.system.cache.manager.sync;

import com.soybeany.mq.broker.BaseMqRegistrySyncerImplB;
import com.soybeany.sync.core.picker.DataPicker;
import com.soybeany.sync.core.picker.DataPickerSimpleImpl;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * @author Soybeany
 * @date 2022/1/22
 */
@Component
public class MqRegistrySyncerImplB extends BaseMqRegistrySyncerImplB {
    @Override
    protected String onSetupSyncUrl(String ip) {
        return getUrl(false, ip, 8083, "", "/bd-api/bSync", "");
    }

    @Override
    public DataPicker<String> onSetupSyncServerPicker() {
        return new DataPickerSimpleImpl<>("http://localhost:8080/bd-api/sync");
    }

    @Override
    public int onSetupSyncIntervalInSec() {
        return 3;
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
