package com.soybeany.system.cache.server.config;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author Soybeany
 * @date 2022/3/18
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(AppConfig.class)
public @interface EnableFileCacheServer {
}
