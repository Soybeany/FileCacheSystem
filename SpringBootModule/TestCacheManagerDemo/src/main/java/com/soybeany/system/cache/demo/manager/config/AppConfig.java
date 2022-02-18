package com.soybeany.system.cache.demo.manager.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;

/**
 * @author Soybeany
 * @date 2022/2/17
 */
@Component
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.soybeany.system.cache.demo.manager.repository")
@EntityScan(basePackages = "com.soybeany.system.cache.demo.manager.model")
public class AppConfig {

}
