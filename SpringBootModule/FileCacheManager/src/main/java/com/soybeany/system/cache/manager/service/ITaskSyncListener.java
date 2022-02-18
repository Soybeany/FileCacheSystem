package com.soybeany.system.cache.manager.service;

/**
 * @author Soybeany
 * @date 2022/2/18
 */
public interface ITaskSyncListener {

    void onConsumerSync(String clientIp, long oldStamp, long newStamp);

}
