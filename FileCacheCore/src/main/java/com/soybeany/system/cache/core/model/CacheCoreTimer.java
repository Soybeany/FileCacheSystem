package com.soybeany.system.cache.core.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Soybeany
 * @date 2020/12/15
 */
public abstract class CacheCoreTimer {

    private ScheduledExecutorService mExecutor;

    protected static long getDelayMillis(LocalDateTime firstTriggerTime, long intervalMillis) {
        LocalDateTime now = LocalDateTime.now();
        long millis = Duration.between(firstTriggerTime, now).toMillis();
        int count = (int) (millis / intervalMillis) + (millis > 0 ? 1 : 0);

        LocalDateTime nextTime = firstTriggerTime.plus((long) count * intervalMillis, ChronoUnit.MILLIS);
        return Duration.between(now, nextTime).toMillis();
    }

    protected void init(boolean inFixRate) {
        long intervalMillis = onSetupIntervalMillis();
        long delayMillis = getDelayMillis(onSetupFirstTriggerTime(), intervalMillis);
        mExecutor = Executors.newSingleThreadScheduledExecutor();
        Runnable task = () -> {
            try {
                onSignal();
            } catch (Throwable e) {
                onException(e);
            }
        };
        if (inFixRate) {
            mExecutor.scheduleAtFixedRate(task, delayMillis, intervalMillis, TimeUnit.MILLISECONDS);
        } else {
            mExecutor.scheduleWithFixedDelay(task, delayMillis, intervalMillis, TimeUnit.MILLISECONDS);
        }
    }

    protected void destroy() {
        if (null != mExecutor) {
            mExecutor.shutdown();
        }
    }

    protected abstract void onSignal();

    protected abstract void onException(Throwable e);

    protected abstract LocalDateTime onSetupFirstTriggerTime();

    protected abstract long onSetupIntervalMillis();

}
