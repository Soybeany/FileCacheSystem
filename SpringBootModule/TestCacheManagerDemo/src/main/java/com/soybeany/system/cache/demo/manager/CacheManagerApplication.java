package com.soybeany.system.cache.demo.manager;

import com.soybeany.mq.broker.anno.EnableBdMqBroker;
import com.soybeany.rpc.provider.anno.EnableBdRpcProvider;
import com.soybeany.system.cache.demo.manager.impl.TaskReceiptHandlerImpl;
import com.soybeany.system.cache.demo.manager.impl.TaskStorageManagerImpl;
import com.soybeany.system.cache.demo.manager.sync.RegistrySyncerImpl;
import com.soybeany.system.cache.manager.config.EnableFileCacheManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * @author Soybeany
 * @date 2020/11/30
 */
@EnableFileCacheManager
@EnableBdMqBroker(
        msgStorageManager = TaskStorageManagerImpl.class,
        receiptHandler = TaskReceiptHandlerImpl.class
)
@EnableBdRpcProvider(syncer = RegistrySyncerImpl.class)
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.soybeany.system.cache.demo.manager.repository")
@EntityScan(basePackages = "com.soybeany.system.cache.demo.manager.model")
@SpringBootApplication
public class CacheManagerApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(CacheManagerApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(CacheManagerApplication.class);
    }

}
