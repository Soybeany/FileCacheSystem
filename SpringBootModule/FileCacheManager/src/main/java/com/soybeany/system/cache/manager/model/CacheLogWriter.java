package com.soybeany.system.cache.manager.model;

import com.soybeany.cache.v2.log.ILogWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Soybeany
 * @date 2020/12/22
 */
public class CacheLogWriter implements ILogWriter {

    private static final Logger LOG = LoggerFactory.getLogger(CacheLogWriter.class);

    @Override
    public void onWriteInfo(String msg) {
        LOG.info(msg);
    }

    @Override
    public void onWriteWarn(String msg) {
        LOG.warn(msg);
    }
}
