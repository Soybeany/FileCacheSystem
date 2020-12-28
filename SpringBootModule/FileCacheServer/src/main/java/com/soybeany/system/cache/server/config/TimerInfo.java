package com.soybeany.system.cache.server.config;

import java.util.Date;

/**
 * @author Soybeany
 * @date 2020/12/14
 */
public class TimerInfo {

    public String name;
    public Date firstTime;
    public long intervalSec;

    public void setName(String name) {
        this.name = name;
    }

    public void setFirstTime(Date firstTime) {
        this.firstTime = firstTime;
    }

    public void setIntervalSec(long intervalSec) {
        this.intervalSec = intervalSec;
    }
}
