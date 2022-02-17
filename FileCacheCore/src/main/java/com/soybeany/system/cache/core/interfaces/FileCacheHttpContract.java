package com.soybeany.system.cache.core.interfaces;

/**
 * @author Soybeany
 * @date 2020/12/14
 */
public interface FileCacheHttpContract {

    // ********************变量********************

    String OPT_PREFIX = "/opt";
    String CLIENT_PREFIX = "/api";

    String TOPIC_TASK_LIST = "bd_file-cache_task-list";

    // *****客户服务器，面向服务器，需授权*****

    String POST_TASK_LIST = OPT_PREFIX + "/postTaskList";

    // *****客户服务器，面向客户端*****

    String GET_FILE_PATH = CLIENT_PREFIX + "/file";

    // *****其它*****

    String AUTHORIZATION = "Authorization";

}
