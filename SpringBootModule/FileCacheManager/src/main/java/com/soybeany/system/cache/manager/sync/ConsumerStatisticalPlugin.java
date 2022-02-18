package com.soybeany.system.cache.manager.sync;

import com.soybeany.mq.broker.api.IStorageManager;
import com.soybeany.mq.broker.plugin.MqBrokerPluginC;
import com.soybeany.mq.core.model.MqConsumerInput;
import com.soybeany.mq.core.model.MqConsumerMsg;
import com.soybeany.mq.core.model.MqConsumerOutput;
import com.soybeany.mq.core.model.MqTopicInfo;
import com.soybeany.sync.core.exception.SyncException;
import com.soybeany.system.cache.core.api.FileCacheHttpContract;
import com.soybeany.system.cache.manager.service.ITaskSyncListener;

import java.util.List;
import java.util.Optional;

/**
 * @author Soybeany
 * @date 2022/2/18
 */
public class ConsumerStatisticalPlugin extends MqBrokerPluginC {

    private final List<ITaskSyncListener> listeners;

    public ConsumerStatisticalPlugin(IStorageManager storageManager, List<ITaskSyncListener> listeners) {
        super(storageManager);
        this.listeners = listeners;
    }

    @Override
    public void onHandleSync(String clientIp, MqConsumerOutput in, MqConsumerInput out) throws SyncException {
        super.onHandleSync(clientIp, in, out);
        // 回调监听器
        MqTopicInfo targetTopic = null;
        for (MqTopicInfo topic : in.getTopics()) {
            if (FileCacheHttpContract.TOPIC_TASK_LIST.equals(topic.getTopic())) {
                targetTopic = topic;
                break;
            }
        }
        if (null == targetTopic) {
            return;
        }
        long oldStamp = targetTopic.getStamp();
        long newStamp = Optional.ofNullable(out.getMessages().get(targetTopic.getTopic()))
                .map(MqConsumerMsg::getStamp).orElse(0L);
        listeners.forEach(listener -> listener.onConsumerSync(clientIp, oldStamp, newStamp));
    }
}
