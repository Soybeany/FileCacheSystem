package com.soybeany.system.cache.demo.manager.service;

import com.soybeany.system.cache.demo.manager.model.CacheServerInfo;
import com.soybeany.system.cache.demo.manager.repository.CacheServerInfoRepository;
import com.soybeany.system.cache.manager.service.ITaskSyncListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author Soybeany
 * @date 2022/2/18
 */
@Service
public class CacheServerSyncServiceImpl implements ITaskSyncListener {

    @Autowired
    private CacheServerInfoRepository cacheServerInfoRepository;

    @Override
    public synchronized void onConsumerSync(String clientIp, long stamp) {
        CacheServerInfo info = cacheServerInfoRepository.findByHost(clientIp).orElseGet(() -> {
            CacheServerInfo newOne = new CacheServerInfo();
            newOne.setHost(clientIp);
            newOne.setAuthorization("不再需要");
            newOne.setDesc("待配置");
            return newOne;
        });
        info.setSyncedTimestamp(new Date(stamp));
        info.setLastSyncTime(new Date());
        cacheServerInfoRepository.save(info);
    }

}
