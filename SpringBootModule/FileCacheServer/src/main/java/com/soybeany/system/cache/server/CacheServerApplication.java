package com.soybeany.system.cache.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * @author Soybeany
 * @date 2020/11/30
 */
@EnableJpaAuditing
@SpringBootApplication
public class CacheServerApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(CacheServerApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(CacheServerApplication.class);
    }

}
