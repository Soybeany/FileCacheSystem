package com.soybeany.system.cache.server.sync;

import com.soybeany.mq.consumer.api.IMqExceptionHandler;
import com.soybeany.mq.consumer.api.IMqMsgHandler;
import com.soybeany.mq.consumer.api.ITopicInfoRepository;
import com.soybeany.mq.consumer.plugin.MqConsumerPlugin;
import com.soybeany.rpc.consumer.BaseRpcConsumerRegistrySyncerImpl;
import com.soybeany.rpc.core.model.RpcServerInfo;
import com.soybeany.sync.client.api.IClientPlugin;
import com.soybeany.sync.client.picker.DataPicker;
import com.soybeany.sync.client.picker.DataPickerSimpleImpl;
import com.soybeany.system.cache.core.api.FileCacheContract;
import com.soybeany.system.cache.server.config.AppConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author Soybeany
 * @date 2021/10/28
 */
@Slf4j
@Component
public class RegistrySyncerImpl extends BaseRpcConsumerRegistrySyncerImpl implements IMqExceptionHandler {

    @Autowired
    private AppConfig appConfig;
    @Autowired
    private List<IMqMsgHandler<?>> handlers;
    @Autowired
    private ITopicInfoRepository topicInfoRepository;

    @Override
    protected void onSetupPlugins(String syncerId, List<IClientPlugin<?, ?>> plugins) {
        super.onSetupPlugins(syncerId, plugins);
        plugins.add(new MqConsumerPlugin(
                Optional.ofNullable(appConfig.getTaskSyncInterval()).orElse(30),
                handlers,
                topicInfoRepository,
                this,
                true
        ));
    }

    @Override
    public DataPicker<RpcServerInfo> onGetNewServerPicker(String serviceId) {
        return new DataPickerSimpleImpl<>();
    }

    @Override
    public void onSetupApiPkgToScan(Set<String> set) {
        set.add(FileCacheContract.API_PKG_TO_SCAN);
    }

    @Override
    public DataPicker<String> onSetupSyncServerPicker() {
        return new DataPickerSimpleImpl<>(appConfig.getRegistryUrls());
    }

    @Override
    protected int onSetupSyncIntervalSec() {
        return Optional.ofNullable(appConfig.getRegistrySyncInterval()).orElse(30);
    }

    @Override
    public boolean onException(String topic, Exception e, long oldStamp, long newStamp, List<?> msgList) {
        log.warn("消息处理异常(" + e.getClass().getSimpleName() + "-" + e.getMessage()
                + ")，topic(" + topic + ")，stamp(" + oldStamp + "->" + newStamp + ")，" + msgList.size() + "条");
        return true;
    }

}
