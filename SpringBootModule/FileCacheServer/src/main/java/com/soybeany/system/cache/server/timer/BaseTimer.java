package com.soybeany.system.cache.server.timer;

import com.soybeany.system.cache.core.model.CacheCoreTimer;
import com.soybeany.system.cache.server.config.TimerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author Soybeany
 * @date 2020/12/15
 */
abstract class BaseTimer extends CacheCoreTimer {

    private static final Logger LOG = LoggerFactory.getLogger(BaseTimer.class);

    @Autowired
    private TimerInfoProvider timerInfoProvider;

    private TimerInfo info;

    @Override
    protected LocalDateTime onSetupFirstTriggerTime() {
        return LocalDateTime.parse(info.referTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    @Override
    protected long onSetupIntervalMillis() {
        return info.intervalSec * 1000;
    }

    @Override
    protected void onException(Throwable e) {
        LOG.warn(getClass().getSimpleName() + "遇到异常:" + e.getMessage());
    }

    @PostConstruct
    public void onInit() {
        info = timerInfoProvider.getTimerInfo(getClass().getSimpleName());
        if (null != info) {
            init(true);
        }
    }

    @PreDestroy
    public void onDestroy() {
        destroy();
    }
}
