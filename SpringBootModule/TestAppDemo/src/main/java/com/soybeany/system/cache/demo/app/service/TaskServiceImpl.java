package com.soybeany.system.cache.demo.app.service;

import com.soybeany.mq.core.exception.MqPluginException;
import com.soybeany.rpc.consumer.api.IRpcServiceProxy;
import com.soybeany.system.cache.app.ITaskSendService;
import com.soybeany.system.cache.core.api.ISecretKeyHolderProvider;
import com.soybeany.system.cache.core.model.CacheTask;
import com.soybeany.system.cache.core.model.FileUid;
import com.soybeany.system.cache.core.model.Payload;
import com.soybeany.system.cache.core.model.SecretKeyHolder;
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
    public String getPayload(String fileToken) throws Exception {
        SecretKeyHolder holder = provider.getHolder();
        SecretKey key = holder.getSecretKey(holder.newestKey);
        return Payload.fromPayload(new Payload(fileToken), key);
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
