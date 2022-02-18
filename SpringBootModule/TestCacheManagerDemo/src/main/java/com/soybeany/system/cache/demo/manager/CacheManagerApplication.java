package com.soybeany.system.cache.demo.manager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * @author Soybeany
 * @date 2020/11/30
 */
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.soybeany.system.cache.demo.manager.repository")
@EntityScan(basePackages = "com.soybeany.system.cache.demo.manager.model")
@SpringBootApplication(scanBasePackages = {"com.soybeany.system"})
public class CacheManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CacheManagerApplication.class, args);
    }

}
