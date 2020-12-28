package com.soybeany.system.cache.core.model;

/**
 * @author Soybeany
 * @date 2020/12/22
 */
public class CacheTask {

    /**
     * 文件标签，表示一个唯一的任务
     */
    public String fileUid;

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

    public void setCanExeFrom(Integer canExeFrom) {
        checkValue(canExeFrom);
        checkRange(canExeFrom, this.canExeTo);
        this.canExeFrom = canExeFrom;
    }

    public int getCanExeTo() {
        return null != canExeTo ? canExeTo : 23;
    }

    public void setCanExeTo(Integer canExeTo) {
        checkValue(canExeTo);
        checkRange(this.canExeFrom, canExeTo);
        this.canExeTo = canExeTo;
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
}
