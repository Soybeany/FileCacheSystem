package com.soybeany.system.cache.demo.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * @author Soybeany
 * @date 2020/11/30
 */
@SpringBootApplication(scanBasePackages = {"com.soybeany.system.cache.demo", "com.soybeany.system.cache.server"})
public class CacheServerApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(CacheServerApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(CacheServerApplication.class);
    }

}
