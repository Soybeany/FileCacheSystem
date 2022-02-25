package com.soybeany.system.cache.core.api;

/**
 * @author Soybeany
 * @date 2020/12/14
 */
public interface FileCacheContract {

    /**
     * 任务推送的主题
     */
    String TOPIC_TASK_LIST = "bd_file-cache_task-list";

    /**
     * 凭证验证使用的header
     */
    String AUTHORIZATION = "Authorization";

    /**
     * 同步器使用的扫描路径
     */
    String API_PKG_TO_SCAN = "com.soybeany.system.cache.core.api";

}
