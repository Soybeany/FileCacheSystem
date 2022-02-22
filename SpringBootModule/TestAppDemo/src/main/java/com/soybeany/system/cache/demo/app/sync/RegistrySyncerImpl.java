package com.soybeany.system.cache.demo.app.sync;

import com.soybeany.mq.core.model.MqProducerMsg;
import com.soybeany.mq.producer.api.IMqMsgSender;
import com.soybeany.mq.producer.plugin.MqProducerPlugin;
import com.soybeany.rpc.core.model.RpcServerInfo;
import com.soybeany.rpc.unit.BaseRpcUnitRegistrySyncerImpl;
import com.soybeany.sync.client.api.IClientPlugin;
import com.soybeany.sync.client.picker.DataPicker;
import com.soybeany.sync.client.picker.DataPickerSimpleImpl;
import com.soybeany.system.cache.core.api.FileCacheContract;
import com.soybeany.system.cache.demo.app.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * @author Soybeany
 * @date 2021/10/28
 */
@Slf4j
@Component
public class RegistrySyncerImpl extends BaseRpcUnitRegistrySyncerImpl implements IMqMsgSender {

    private MqProducerPlugin producerPlugin;

    @Override
    protected String onSetupGroup() {
        return TaskService.GROUP;
    }

    @Override
    protected void onSetupPlugins(List<IClientPlugin<?, ?>> plugins) {
        super.onSetupPlugins(plugins);
        plugins.add(producerPlugin = new MqProducerPlugin(this));
    }

    @Override
    public <T extends Serializable> void send(String topic, MqProducerMsg<T> msg) {
        producerPlugin.send(topic, msg);
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
    protected void onSetupApiPkgToScan(Set<String> set) {
        set.add(FileCacheContract.PKG_PATH_TO_SCAN);
    }

    @Override
    protected void onSetupImplPkgToScan(Set<String> set) {
        set.add(FileCacheContract.PKG_PATH_TO_SCAN);
    }

    @Override
    public DataPicker<String> onSetupSyncServerPicker() {
        return new DataPickerSimpleImpl<>("http://localhost:8180/bd-api/sync");
    }

    @Override
    protected int onSetupSyncIntervalSec() {
        return 5;
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
