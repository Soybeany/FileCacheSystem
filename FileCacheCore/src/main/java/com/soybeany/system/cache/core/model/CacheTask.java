package com.soybeany.system.cache.core.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

/**
 * @author Soybeany
 * @date 2020/12/22
 */
@RequiredArgsConstructor
public class CacheTask implements Serializable {

    /**
     * 文件标签，表示一个唯一的任务
     */
    @Getter
    private final String fileUid;

    /**
     * 可执行的开始时间(小时:0~23)
     */
    private Integer canExeFrom;

    /**
     * 可执行的结束时间(小时:0~23)
     */
    private Integer canExeTo;

    public int getCanExeFrom() {
        return null != canExeFrom ? canExeFrom : 0;
    }

    public CacheTask canExeFrom(Integer canExeFrom) {
        checkValue(canExeFrom);
        checkRange(canExeFrom, this.canExeTo);
        this.canExeFrom = canExeFrom;
        return this;
    }

    public int getCanExeTo() {
        return null != canExeTo ? canExeTo : 23;
    }

    public CacheTask canExeTo(Integer canExeTo) {
        checkValue(canExeTo);
        checkRange(this.canExeFrom, canExeTo);
        this.canExeTo = canExeTo;
        return this;
    }

    private void checkValue(Integer value) {
        if (null == value) {
            return;
        }
        if (value < 0 || value > 23) {
            throw new RuntimeException("不允许的范围值");
        }
    }

    private void checkRange(Integer canExeFrom, Integer canExeTo) {
        if (null != canExeFrom && null != canExeTo && canExeFrom > canExeTo) {
            throw new RuntimeException("时间范围定义有误");
        }
    }

    public static class WithStamp extends CacheTask {
        /**
         * 标识任务所属的戳
         */
        @Getter
        private final long stamp;

        public WithStamp(String fileUid, long stamp) {
            super(fileUid);
            this.stamp = stamp;
        }
    }

}
