package com.soybeany.system.cache.demo.manager.service;

import com.soybeany.mq.broker.api.IStorageManager;
import com.soybeany.mq.core.model.MqConsumerMsg;
import com.soybeany.mq.core.model.MqProducerMsg;
import com.soybeany.mq.core.model.MqTopicInfo;
import com.soybeany.system.cache.demo.manager.repository.TaskInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author Soybeany
 * @date 2022/2/18
 */
@Service
public class TaskStorageServiceImpl implements IStorageManager {

    @Autowired
    private TaskInfoRepository taskInfoRepository;

    @Override
    public void save(Map<String, List<MqProducerMsg>> map) throws Exception {

    }

    @Override
    public Map<String, MqConsumerMsg> load(List<MqTopicInfo> list) throws Exception {
        return null;
    }
}
