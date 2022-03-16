package com.soybeany.system.cache.demo.app.impl;

import com.soybeany.system.cache.app.impl.BaseTaskServiceImpl;
import com.soybeany.system.cache.demo.app.config.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Soybeany
 * @date 2022/3/16
 */
@Component
public class TaskServiceImpl extends BaseTaskServiceImpl {

    @Autowired
    private AppConfig appConfig;

    @Override
    protected String onSetupGroup() {
        return appConfig.getGroup();
    }

}
