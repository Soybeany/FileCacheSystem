package com.soybeany.system.cache.server.sync;

import com.soybeany.mq.consumer.BaseMqConsumerBrokerSyncerImpl;
import com.soybeany.mq.consumer.api.IMqExceptionHandler;
import com.soybeany.mq.consumer.api.IMqMsgHandler;
import com.soybeany.mq.consumer.api.ITopicInfoRepository;
import com.soybeany.mq.consumer.impl.TopicInfoRepositoryMemImpl;
import com.soybeany.mq.core.api.IMqBrokerSyncUrlProvider;
import com.soybeany.rpc.consumer.api.IRpcServiceProxy;
import com.soybeany.system.cache.server.config.AppConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Optional;

/**
 * @author Soybeany
 * @date 2022/1/20
 */
@Slf4j
@Component
public class MqBrokerSyncerImpl extends BaseMqConsumerBrokerSyncerImpl {

    @Autowired
    private AppConfig appConfig;
    @Autowired
    private List<IMqMsgHandler> handlers;
    @Autowired
    private IRpcServiceProxy serviceProxy;

    @Override
    protected List<IMqMsgHandler> onSetupMsgHandlers() {
        return handlers;
    }

    @Override
    protected ITopicInfoRepository onSetupTopicInfoRepository() {
        return new TopicInfoRepositoryMemImpl();
    }

    @Override
    protected IMqExceptionHandler onSetupExceptionHandler() {
        return (e, s, iMqMsgHandler) -> {
            log.warn("出现异常:" + e.getMessage());
            return false;
        };
    }

    @Override
    protected IMqBrokerSyncUrlProvider onGetMqBrokerSyncUrlProvider() {
        return serviceProxy.get(IMqBrokerSyncUrlProvider.class);
    }

    @Override
    public int onSetupSyncIntervalInSec() {
        return Optional.ofNullable(appConfig.getTaskSyncInterval()).orElse(30);
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
