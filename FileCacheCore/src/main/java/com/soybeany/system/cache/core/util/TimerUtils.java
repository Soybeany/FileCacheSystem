package com.soybeany.system.cache.core.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class TimerUtils {

    public static ScheduledExecutorService getTimer(int intervalSec, Runnable onExecute) {
        return getTimer(intervalSec, onExecute, e -> {

        });
    }

    public static ScheduledExecutorService getTimer(int intervalSec, Runnable onExecute, Consumer<Throwable> onException) {
        ScheduledExecutorService result = Executors.newSingleThreadScheduledExecutor();
        result.scheduleWithFixedDelay(() -> {
            try {
                onExecute.run();
            } catch (Throwable e) {
                onException.accept(e);
            }
        }, 0, intervalSec, TimeUnit.SECONDS);
        return result;
    }

}
