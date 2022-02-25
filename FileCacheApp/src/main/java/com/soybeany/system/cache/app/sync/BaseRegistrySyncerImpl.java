package com.soybeany.system.cache.app.sync;

import com.soybeany.mq.core.model.MqProducerMsg;
import com.soybeany.mq.producer.api.IMqMsgSender;
import com.soybeany.mq.producer.plugin.MqProducerPlugin;
import com.soybeany.rpc.unit.BaseRpcUnitRegistrySyncerImpl;
import com.soybeany.sync.client.api.IClientPlugin;
import com.soybeany.system.cache.app.service.TaskService;
import com.soybeany.system.cache.core.api.FileCacheContract;
import com.soybeany.system.cache.core.api.ISecretKeyHolderProvider;
import com.soybeany.system.cache.core.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.crypto.SecretKey;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.soybeany.system.cache.core.api.FileCacheContract.TOPIC_TASK_LIST;

/**
 * @author Soybeany
 * @date 2021/10/28
 */
public abstract class BaseRegistrySyncerImpl extends BaseRpcUnitRegistrySyncerImpl implements IMqMsgSender, TaskService {

    @Lazy
    @Autowired
    private IMqMsgSender sender;

    private String group;
    private MqProducerPlugin producerPlugin;
    private ISecretKeyHolderProvider provider;

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
    protected void onSetupApiPkgToScan(Set<String> set) {
        set.add(FileCacheContract.API_PKG_TO_SCAN);
    }

    @Override
    public String getToken(String fileToken) throws Exception {
        SecretKeyHolder holder = provider.getHolder();
        String key = holder.newestKey;
        SecretKey secretKey = holder.getSecretKey(key);
        String payload = Payload.fromPayload(new Payload(fileToken), secretKey);
        return TokenPart.toToken(new TokenPart(group, key, payload));
    }

    @Override
    public void postTask(String fileToken) {
        LocalDateTime now = LocalDateTime.now();
        // todo 管理器的实体结构不支持起始时间、结束时间的配置，故时间随意设置
        ArrayList<CacheTask> tasks = new ArrayList<>();
        tasks.add(new CacheTask(FileUid.toFileUid(group, fileToken)));
        sender.send(TOPIC_TASK_LIST, new MqProducerMsg<>(now, now.plusMonths(1), tasks));
    }

    @PostConstruct
    private void onInit() {
        start();
        group = onSetupGroup();
        provider = get(ISecretKeyHolderProvider.class);
    }

    @PreDestroy
    private void onDestroy() {
        stop();
    }


}
