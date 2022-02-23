package com.soybeany.system.cache.app;

/**
 * @author Soybeany
 * @date 2022/2/19
 */
public interface TaskService {

    String getToken(String fileToken) throws Exception;

    void postTask(String fileToken);

}
