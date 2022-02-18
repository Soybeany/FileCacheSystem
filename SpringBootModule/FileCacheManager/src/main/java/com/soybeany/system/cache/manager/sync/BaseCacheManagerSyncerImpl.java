package com.soybeany.system.cache.manager.sync;

import com.soybeany.mq.broker.api.IStorageManager;
import com.soybeany.mq.broker.plugin.MqBrokerPluginP;
import com.soybeany.mq.core.api.IMqBrokerSyncUrlProvider;
import com.soybeany.sync.core.util.NetUtils;
import com.soybeany.sync.server.api.IServerPlugin;
import com.soybeany.sync.server.impl.BaseServerSyncerImpl;
import com.soybeany.system.cache.manager.service.ITaskSyncListener;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author Soybeany
 * @date 2022/2/18
 */
public abstract class BaseCacheManagerSyncerImpl extends BaseServerSyncerImpl implements IMqBrokerSyncUrlProvider {

    @Autowired(required = false)
    private List<ITaskSyncListener> listeners;

    @Override
    protected void onSetupPlugins(List<IServerPlugin<?, ?>> plugins) {
        IStorageManager manager = onSetupStorageManager();
        plugins.add(new MqBrokerPluginP(manager));
        plugins.add(new ConsumerStatisticalPlugin(manager, Optional.ofNullable(listeners).orElse(Collections.emptyList())));
    }

    @Override
    public String onGetSyncUrl() {
        return this.onSetupSyncUrl(NetUtils.getLocalIpAddress());
    }

    protected abstract String onSetupSyncUrl(String var1);

    protected abstract IStorageManager onSetupStorageManager();
}
