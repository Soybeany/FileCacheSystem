package com.soybeany.system.cache.demo.manager.service;

import com.soybeany.mq.core.api.IMqMsgStorageManager;
import com.soybeany.mq.core.model.MqConsumerMsg;
import com.soybeany.mq.core.model.MqProducerMsg;
import com.soybeany.mq.core.model.MqTopicInfo;
import com.soybeany.system.cache.core.model.CacheTask;
import com.soybeany.system.cache.core.util.CacheCoreTimeUtils;
import com.soybeany.system.cache.demo.manager.model.TaskInfo;
import com.soybeany.system.cache.demo.manager.repository.TaskInfoRepository;
import com.soybeany.system.cache.manager.config.UserConfig;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

import static com.soybeany.system.cache.core.api.FileCacheContract.TOPIC_TASK_LIST;

/**
 * @author Soybeany
 * @date 2022/2/18
 */
@Service
public class TaskStorageServiceImpl implements IMqMsgStorageManager {

    @Autowired
    private UserConfig userConfig;
    @Autowired
    private TaskInfoRepository taskInfoRepository;

    @Transactional
    @Override
    public synchronized <T extends Serializable> void save(String topic, MqProducerMsg<T> msg) {
        if (!TOPIC_TASK_LIST.equals(topic)) {
            return;
        }
        // todo 实体结构不支持起始时间、结束时间的配置，故不起作用
        taskInfoRepository.saveAll(toTasks(msg.getMsg()));
    }

    @Override
    public synchronized <T extends Serializable> Map<String, MqConsumerMsg<T>> load(Collection<MqTopicInfo> topics) {
        Map<String, MqConsumerMsg<T>> result = new HashMap<>();
        MqTopicInfo targetInfo = null;
        for (MqTopicInfo info : topics) {
            if (TOPIC_TASK_LIST.equals(info.getTopic())) {
                targetInfo = info;
                break;
            }
        }
        if (null == targetInfo) {
            return result;
        }
        Date stampDate = getStampDate(targetInfo.getStamp());
        List<TaskInfo> tasks = taskInfoRepository.findByLastModifyTimeAfterOrderByLastModifyTime(stampDate);
        return toMsgMap(result, tasks);
    }

    // ***********************内部方法****************************

    @SuppressWarnings("unchecked")
    private <T extends Serializable> Map<String, MqConsumerMsg<T>> toMsgMap(Map<String, MqConsumerMsg<T>> result, List<TaskInfo> tasks) {
        if (tasks.isEmpty()) {
            return result;
        }
        MqConsumerMsg<T> msg = new MqConsumerMsg<>();
        msg.setStamp(tasks.get(tasks.size() - 1).getLastModifyTime().getTime());
        for (TaskInfo task : tasks) {
            CacheTask cacheTask = new CacheTask(task.getFileUid())
                    .canExeFrom(task.getCanExeFrom())
                    .canExeTo(task.getCanExeTo());
            msg.getMsgList().add((T) cacheTask);
        }
        result.put(TOPIC_TASK_LIST, msg);
        return result;
    }

    private Date getStampDate(long referStamp) {
        LocalDateTime minTime = LocalDateTime.now().minusDays(userConfig.getTaskSyncMaxDay());
        long minStamp = CacheCoreTimeUtils.toMillis(minTime);
        return new Date(Math.max(minStamp, referStamp));
    }

    @SuppressWarnings("unchecked")
    private <T extends Serializable> List<TaskInfo> toTasks(T obj) {
        List<CacheTask> tasks = (List<CacheTask>) obj;
        List<TaskInfo> result = new ArrayList<>();
        for (CacheTask task : tasks) {
            TaskInfo info = Optional.ofNullable(taskInfoRepository.findByFileUid(task.getFileUid()))
                    .orElseGet(TaskInfo::new);
            BeanUtils.copyProperties(task, info);
            result.add(info);
        }
        return result;
    }

}
