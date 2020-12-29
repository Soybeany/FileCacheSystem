package com.soybeany.system.cache.server.config;

/**
 * @author Soybeany
 * @date 2020/12/14
 */
public class TimerInfo {

    public String name;
    public String referTime;
    public long intervalSec;

    public void setName(String name) {
        this.name = name;
    }

    public void setReferTime(String referTime) {
        this.referTime = referTime;
    }

    public void setIntervalSec(long intervalSec) {
        this.intervalSec = intervalSec;
    }
}
