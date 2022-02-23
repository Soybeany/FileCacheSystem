package com.soybeany.system.cache.demo.app.service;

import com.soybeany.mq.core.exception.MqPluginException;
import com.soybeany.rpc.consumer.api.IRpcServiceProxy;
import com.soybeany.system.cache.app.ITaskSendService;
import com.soybeany.system.cache.core.api.ISecretKeyHolderProvider;
import com.soybeany.system.cache.core.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;

/**
 * @author Soybeany
 * @date 2022/2/19
 */
@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    private IRpcServiceProxy serviceProxy;
    @Autowired
    private ITaskSendService taskSendService;

    private ISecretKeyHolderProvider provider;

    @Override
    public String getToken(String fileToken) throws Exception {
        SecretKeyHolder holder = provider.getHolder();
        String key = holder.newestKey;
        SecretKey secretKey = holder.getSecretKey(key);
        String payload = Payload.fromPayload(new Payload(fileToken), secretKey);
        return TokenPart.toToken(new TokenPart(TaskService.GROUP, key, payload));
    }

    @Override
    public void postTask(String fileToken) throws MqPluginException {
        taskSendService.send(new CacheTask(FileUid.toFileUid(GROUP, fileToken)));
    }

    // ***********************内部方法****************************

    @PostConstruct
    private void onInit() {
        provider = serviceProxy.get(ISecretKeyHolderProvider.class);
    }

}
