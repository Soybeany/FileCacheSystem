package com.soybeany.system.cache.app;

import com.soybeany.mq.core.exception.MqPluginException;
import com.soybeany.mq.core.model.MqProducerMsg;
import com.soybeany.mq.producer.api.IMqMsgSender;
import com.soybeany.system.cache.core.model.CacheTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static com.soybeany.system.cache.core.api.FileCacheContract.TOPIC_TASK_LIST;

/**
 * @author Soybeany
 * @date 2022/1/26
 */
@Component
public class TaskSendServiceImpl implements ITaskSendService {

    @Autowired
    private IMqMsgSender sender;

    @Override
    public void send(CacheTask task) throws MqPluginException {
        LocalDateTime now = LocalDateTime.now();
        // todo 管理器的实体结构不支持起始时间、结束时间的配置，故时间随意设置
        sender.syncSend(TOPIC_TASK_LIST, MqProducerMsg.createWithJsonMsg(now, now.plusMonths(1), task));
    }

}
