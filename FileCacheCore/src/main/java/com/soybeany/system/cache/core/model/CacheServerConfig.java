package com.soybeany.system.cache.core.model;

import lombok.Data;

/**
 * @author Soybeany
 * @date 2022/2/16
 */
@Data
public class CacheServerConfig {

    private final String cacheFileDir;
    private final String tempFileDir;
    private final int cleanIntervalSec;
    private final int taskExeIntervalSec;
    private final int lockTimeoutSec;
    private final int downloadTimeoutSec;
    private final int taskConcurrentMaxCount;
    private final int taskRetryCount;

}
