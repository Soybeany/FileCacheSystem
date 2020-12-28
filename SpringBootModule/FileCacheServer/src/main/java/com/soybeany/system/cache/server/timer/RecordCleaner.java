package com.soybeany.system.cache.server.timer;

import com.soybeany.system.cache.server.service.CleanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Soybeany
 * @date 2020/12/16
 */
@Component
class RecordCleaner extends BaseTimer {

    private static final Logger LOG = LoggerFactory.getLogger(FileCleaner.class);

    @Autowired
    private CleanService cleanService;

    @Override
    protected void onSignal() {
        int count = cleanService.cleanRecords();
        LOG.info("清理了" + count + "条记录");
    }

}
