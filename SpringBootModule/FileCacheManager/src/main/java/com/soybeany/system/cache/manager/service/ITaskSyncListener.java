package com.soybeany.system.cache.manager.service;

import com.soybeany.mq.core.model.MqTopicInfo;

import java.util.List;

/**
 * @author Soybeany
 * @date 2022/2/18
 */
public interface ITaskSyncListener {

    void onConsumerSync(String clientIp, List<MqTopicInfo> topics);

}
