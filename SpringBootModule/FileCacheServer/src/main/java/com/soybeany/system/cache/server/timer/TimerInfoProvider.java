package com.soybeany.system.cache.server.timer;

import com.soybeany.system.cache.server.config.AppConfig;
import com.soybeany.system.cache.server.config.TimerInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Soybeany
 * @date 2020/12/16
 */
@Component
class TimerInfoProvider {

    @Autowired
    private AppConfig appConfig;

    private final Map<String, TimerInfo> map = new HashMap<>();

    @PostConstruct
    public void onPostConstruct() {
        for (TimerInfo info : appConfig.timers) {
            map.put(info.name, info);
        }
    }

    public TimerInfo getTimerInfo(String key) {
        return map.get(key);
    }
}
