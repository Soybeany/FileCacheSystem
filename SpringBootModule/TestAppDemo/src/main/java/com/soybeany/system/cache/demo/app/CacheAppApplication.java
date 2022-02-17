package com.soybeany.system.cache.demo.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Soybeany
 * @date 2020/11/30
 */
@SpringBootApplication(scanBasePackages = {"com.soybeany.system"})
public class CacheAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(CacheAppApplication.class, args);
    }

}
