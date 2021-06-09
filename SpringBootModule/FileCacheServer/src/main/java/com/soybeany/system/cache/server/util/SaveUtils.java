package com.soybeany.system.cache.server.util;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Soybeany
 * @date 2021/6/9
 */
public class SaveUtils {

    /**
     * 线性持久化，以防sqlite的文件锁导致存储失败
     */
    public static synchronized <T> T syncSave(JpaRepository<T, ?> repository, T obj) {
        return repository.save(obj);
    }

}
