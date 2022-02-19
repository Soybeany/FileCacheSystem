package com.soybeany.system.cache.demo.manager.service;

import com.google.gson.Gson;
import com.soybeany.mq.broker.api.IStorageManager;
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

import java.time.LocalDateTime;
import java.util.*;

import static com.soybeany.system.cache.core.api.FileCacheContract.TOPIC_TASK_LIST;

/**
 * @author Soybeany
 * @date 2022/2/18
 */
@Service
public class TaskStorageServiceImpl implements IStorageManager {

    private static final Gson GSON = new Gson();

    @Autowired
    private UserConfig userConfig;
    @Autowired
    private TaskInfoRepository taskInfoRepository;

    @Transactional
    @Override
    public synchronized void save(Map<String, List<MqProducerMsg>> map) {
        // todo 实体结构不支持起始时间、结束时间的配置，故不起作用
        List<TaskInfo> tasks = new ArrayList<>();
        Optional.ofNullable(map.get(TOPIC_TASK_LIST))
                .ifPresent(list -> list.forEach(msg -> tasks.add(toTask(msg.getMsg()))));
        taskInfoRepository.saveAll(tasks);
    }

    @Override
    public Map<String, MqConsumerMsg> load(List<MqTopicInfo> list) {
        Map<String, MqConsumerMsg> result = new HashMap<>();
        MqTopicInfo targetInfo = null;
        for (MqTopicInfo info : list) {
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

    private Map<String, MqConsumerMsg> toMsgMap(Map<String, MqConsumerMsg> result, List<TaskInfo> tasks) {
        if (tasks.isEmpty()) {
            return result;
        }
        MqConsumerMsg msg = new MqConsumerMsg();
        msg.setStamp(tasks.get(tasks.size() - 1).getLastModifyTime().getTime());
        for (TaskInfo task : tasks) {
            msg.getMessages().add(GSON.toJson(task, CacheTask.class));
        }

        result.put(TOPIC_TASK_LIST, msg);
        return result;
    }

    private Date getStampDate(long referStamp) {
        LocalDateTime minTime = LocalDateTime.now().minusDays(userConfig.getTaskSyncMaxDay());
        long minStamp = CacheCoreTimeUtils.toMillis(minTime);
        return new Date(Math.max(minStamp, referStamp));
    }

    private TaskInfo toTask(String json) {
        CacheTask task = GSON.fromJson(json, CacheTask.class);
        TaskInfo info = Optional.ofNullable(taskInfoRepository.findByFileUid(task.getFileUid()))
                .orElseGet(TaskInfo::new);
        BeanUtils.copyProperties(task, info);
        return info;
    }

}
