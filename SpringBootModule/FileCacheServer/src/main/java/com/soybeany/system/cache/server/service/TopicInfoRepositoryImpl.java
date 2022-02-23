package com.soybeany.system.cache.server.service;

import com.soybeany.mq.consumer.api.ITopicInfoRepository;
import com.soybeany.mq.core.model.MqTopicInfo;
import com.soybeany.system.cache.core.api.FileCacheContract;
import com.soybeany.system.cache.server.repository.DbDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

/**
 * @author Soybeany
 * @date 2022/2/23
 */
@Service
public class TopicInfoRepositoryImpl implements ITopicInfoRepository {

    @Autowired
    private DbDAO dbDAO;

    @Override
    public Collection<MqTopicInfo> getAll(Collection<String> collection) {
        Long stamp = dbDAO.selectTaskInfoNewestStamp();
        return Collections.singleton(new MqTopicInfo(FileCacheContract.TOPIC_TASK_LIST, stamp));
    }

    @Override
    public void updateTopicInfo(MqTopicInfo mqTopicInfo) {
        // 不需额外做操作
    }

}
