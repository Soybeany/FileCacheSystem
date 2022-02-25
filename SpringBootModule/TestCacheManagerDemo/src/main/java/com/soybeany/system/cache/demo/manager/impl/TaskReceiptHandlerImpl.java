package com.soybeany.system.cache.demo.manager.impl;

import com.soybeany.system.cache.manager.service.BaseTaskReceiptHandler;
import com.soybeany.system.cache.manager.service.ITaskSyncListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Soybeany
 * @date 2022/2/25
 */
@Component
public class TaskReceiptHandlerImpl extends BaseTaskReceiptHandler {
    public TaskReceiptHandlerImpl(List<ITaskSyncListener> listeners) {
        super(listeners);
    }
}
