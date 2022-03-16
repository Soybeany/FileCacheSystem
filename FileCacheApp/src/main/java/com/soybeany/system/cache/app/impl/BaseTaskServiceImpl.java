package com.soybeany.system.cache.app.impl;

import com.soybeany.mq.core.model.MqProducerMsg;
import com.soybeany.mq.producer.api.IMqMsgSender;
import com.soybeany.rpc.consumer.api.IRpcServiceProxy;
import com.soybeany.system.cache.app.service.TaskService;
import com.soybeany.system.cache.core.api.ISecretKeyHolderProvider;
import com.soybeany.system.cache.core.model.*;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.soybeany.system.cache.core.api.FileCacheContract.TOPIC_TASK_LIST;

/**
 * @author Soybeany
 * @date 2022/3/16
 */
public abstract class BaseTaskServiceImpl implements TaskService, InitializingBean {

    @Autowired
    private IRpcServiceProxy proxy;
    @Autowired
    private IMqMsgSender msgSender;

    private String group;
    private ISecretKeyHolderProvider provider;

    @Override
    public String getToken(String fileToken) throws Exception {
        SecretKeyHolder holder = provider.getHolder();
        String key = holder.newestKey;
        SecretKey secretKey = holder.getSecretKey(key);
        String payload = Payload.fromPayload(new Payload(fileToken), secretKey);
        return TokenPart.toToken(new TokenPart(group, key, payload));
    }

    @Override
    public void postTasks(List<String> fileTokens, TaskConfigurer configurer) {
        LocalDateTime now = LocalDateTime.now();
        ArrayList<CacheTask> tasks = new ArrayList<>();
        for (String fileToken : fileTokens) {
            CacheTask task = new CacheTask(FileUid.toFileUid(group, fileToken));
            configurer.onConfigure(task);
            tasks.add(task);
        }
        msgSender.send(TOPIC_TASK_LIST, new MqProducerMsg<>(now, now.plusMonths(1), tasks));
    }

    @Override
    public void afterPropertiesSet() {
        group = onSetupGroup();
        provider = proxy.get(ISecretKeyHolderProvider.class);
    }

    protected abstract String onSetupGroup();

}
