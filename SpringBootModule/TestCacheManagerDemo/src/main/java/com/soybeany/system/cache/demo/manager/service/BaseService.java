package com.soybeany.system.cache.demo.manager.service;

import com.soybeany.system.cache.demo.manager.repository.CacheServerInfo;
import com.soybeany.system.cache.demo.manager.repository.CacheServerInfoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author Soybeany
 * @date 2020/12/24
 */
abstract class BaseService {

    private static final Logger LOG = LoggerFactory.getLogger(BaseService.class);

    @Autowired
    private CacheServerInfoRepository cacheServerInfoRepository;

    private ExecutorService executorService;

    @SuppressWarnings("AlibabaThreadPoolCreation")
    @PostConstruct
    void onInit() {
        executorService = Executors.newCachedThreadPool();
    }

    @PreDestroy
    void onDestroy() {
        executorService.shutdown();
    }

    // 任务同步
    protected <T> void execute(String desc, IExecuteCallback<T> executeCallback, IFinishCallback<T> finishCallback) {
        List<Result<T>> results = new LinkedList<>();
        List<CacheServerInfo> serverInfoList = cacheServerInfoRepository.findAll();
        if (serverInfoList.isEmpty()) {
            LOG.info(desc + "没有找到缓存服务器");
            return;
        }
        for (CacheServerInfo info : serverInfoList) {
            Future<T> future = executorService.submit(() -> executeCallback.onSignal(info));
            results.add(new Result<>(info, future));
        }
        for (Result<T> result : results) {
            try {
                finishCallback.onSignal(result.info, result.future.get());
            } catch (Exception e) {
                LOG.warn("“" + result.info.desc + "”" + desc + "异常:“" + e.getMessage());
            }
        }
    }

    protected boolean needExecute(Date lastExecuteTime, long intervalSec) {
        if (null == lastExecuteTime) {
            return true;
        }
        // 没到同步间隔，则不同步
        long curTime = System.currentTimeMillis();
        long startTime = lastExecuteTime.getTime();
        return curTime - startTime > intervalSec * 1000;
    }

    protected interface IExecuteCallback<T> {
        T onSignal(CacheServerInfo info) throws Exception;
    }

    protected interface IFinishCallback<T> {
        void onSignal(CacheServerInfo info, T result);
    }

    private static class Result<T> {
        final CacheServerInfo info;
        final Future<T> future;

        public Result(CacheServerInfo info, Future<T> future) {
            this.info = info;
            this.future = future;
        }
    }
}
