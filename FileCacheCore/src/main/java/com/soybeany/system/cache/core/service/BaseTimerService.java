package com.soybeany.system.cache.core.service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Soybeany
 * @date 2020/12/15
 */
public abstract class BaseTimerService {

    private static final int DEFAULT_DELAY = 60;

    private ScheduledExecutorService mExecutor;

    @SuppressWarnings("AlibabaThreadPoolCreation")
    protected void init() {
        mExecutor = Executors.newSingleThreadScheduledExecutor();
        execute(DEFAULT_DELAY / 2);
    }

    protected void destroy() {
        if (null != mExecutor) {
            mExecutor.shutdown();
        }
    }

    protected abstract void onSignal();

    protected abstract int onSetupIntervalSec();

    // ***********************内部方法****************************

    private void execute(int delay) {
        mExecutor.schedule(() -> {
            try {
                onSignal();
            } finally {
                execute(getDelay());
            }
        }, delay, TimeUnit.SECONDS);
    }

    private int getDelay() {
        int delay;
        try {
            delay = onSetupIntervalSec();
        } catch (Exception e) {
            delay = DEFAULT_DELAY;
        }
        return delay;
    }

}
