package com.soybeany.system.cache.demo.manager.service;

import com.soybeany.mq.core.model.MqTopicInfo;
import com.soybeany.system.cache.core.api.FileCacheHttpContract;
import com.soybeany.system.cache.demo.manager.model.CacheServerInfo;
import com.soybeany.system.cache.demo.manager.repository.CacheServerInfoRepository;
import com.soybeany.system.cache.manager.service.ITaskSyncListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * @author Soybeany
 * @date 2022/2/18
 */
@Service
public class CacheServerSyncServiceImpl implements ITaskSyncListener {

    @Autowired
    private CacheServerInfoRepository cacheServerInfoRepository;

    @Transactional
    @Override
    public synchronized void onConsumerSync(String clientIp, List<MqTopicInfo> topics) {
        for (MqTopicInfo topic : topics) {
            if (!FileCacheHttpContract.TOPIC_TASK_LIST.equals(topic.getTopic())) {
                continue;
            }
            onSaveSyncRecord(clientIp, topic.getStamp());
        }
    }

    // ***********************内部方法****************************

    private void onSaveSyncRecord(String clientIp, long stamp) {
        CacheServerInfo info = cacheServerInfoRepository.findByHost(clientIp).orElseGet(() -> {
            CacheServerInfo newOne = new CacheServerInfo();
            newOne.setHost(clientIp);
            newOne.setAuthorization("不再需要");
            return newOne;
        });
        info.setLastSyncTime(new Date(stamp));
        info.setLastSyncTime(new Date());
        cacheServerInfoRepository.save(info);
    }

}
