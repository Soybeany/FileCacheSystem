package com.soybeany.system.cache.app;

import com.soybeany.mq.core.exception.MqPluginException;
import com.soybeany.system.cache.core.model.CacheTask;

/**
 * @author Soybeany
 * @date 2022/1/26
 */
public interface ITaskSendService {

    void send(CacheTask task) throws MqPluginException;

}
