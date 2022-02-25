package com.soybeany.system.cache.manager.service;

import com.soybeany.mq.core.api.IMqReceiptHandler;
import com.soybeany.mq.core.model.MqReceiptInfo;
import com.soybeany.system.cache.core.api.FileCacheContract;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.List;

/**
 * @author Soybeany
 * @date 2022/2/22
 */
@Slf4j
@RequiredArgsConstructor
public abstract class BaseTaskReceiptHandler implements IMqReceiptHandler {

    private final List<ITaskSyncListener> listeners;

    @Override
    public void onSuccess(String clientIp, Collection<MqReceiptInfo> list) {
        // 回调监听器
        MqReceiptInfo targetInfo = null;
        for (MqReceiptInfo info : list) {
            if (FileCacheContract.TOPIC_TASK_LIST.equals(info.getTopic())) {
                targetInfo = info;
                break;
            }
        }
        if (null == targetInfo) {
            return;
        }
        Long newStamp = targetInfo.getNewStamp();
        listeners.forEach(listener -> listener.onConsumerSync(clientIp, newStamp));
    }

}
