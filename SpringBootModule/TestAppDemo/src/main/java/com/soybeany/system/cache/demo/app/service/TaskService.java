package com.soybeany.system.cache.demo.app.service;

import com.soybeany.mq.core.exception.MqPluginException;

/**
 * @author Soybeany
 * @date 2022/2/19
 */
public interface TaskService {

    String GROUP = "efb";

    String getPayload(String fileToken) throws Exception;

    void postTask(String fileToken) throws MqPluginException;

}
