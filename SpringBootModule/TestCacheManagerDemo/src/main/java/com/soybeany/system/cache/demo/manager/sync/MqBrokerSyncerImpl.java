package com.soybeany.system.cache.demo.manager.sync;

import com.soybeany.mq.broker.api.IStorageManager;
import com.soybeany.mq.broker.impl.StorageManagerMemImpl;
import com.soybeany.system.cache.manager.sync.BaseCacheManagerSyncerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * @author Soybeany
 * @date 2022/1/20
 */
@Component
public class MqBrokerSyncerImpl extends BaseCacheManagerSyncerImpl {

    @Autowired
    private IStorageManager manager;

    @Override
    protected String onSetupSyncUrl(String ip) {
        return getUrl(false, ip, 8182, "", "/sync/broker", "");
    }

    @Override
    protected IStorageManager onSetupStorageManager() {
        return new StorageManagerMemImpl();
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
