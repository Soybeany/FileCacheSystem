package com.soybeany.system.cache.manager.config;

import com.soybeany.system.cache.core.security.model.MapRepositoryImpl;
import com.soybeany.system.cache.core.security.model.SecretKeyProvider;
import com.soybeany.system.cache.core.task.CacheTask;
import com.soybeany.system.cache.manager.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author Soybeany
 * @date 2020/12/29
 */
@Configuration
class AppConfig {

    @ConditionalOnMissingBean
    @Bean
    SecretKeyProvider.Repository defaultSecretKeyRepository() {
        return new MapRepositoryImpl();
    }

    @ConditionalOnMissingBean
    @Bean
    TaskService defaultTaskService() {
        return new DefaultTaskServiceImpl();
    }

    private static class DefaultTaskServiceImpl implements TaskService {
        private final Logger LOG = LoggerFactory.getLogger(DefaultTaskServiceImpl.class);

        @Override
        public void saveTasks(List<CacheTask> tasks) {
            LOG.warn("没有实际保存任务，请自定义TaskService的实现类");
        }

        @Override
        public void onSyncTasks() {
            LOG.warn("没有实际同步任务，请自定义TaskService的实现类");
        }

        @Override
        public void onCleanTasks() {
            LOG.warn("没有实际清理任务，请自定义TaskService的实现类");
        }
    }
}
