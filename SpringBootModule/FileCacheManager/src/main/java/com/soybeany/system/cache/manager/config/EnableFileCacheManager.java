package com.soybeany.system.cache.manager.config;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author Soybeany
 * @date 2022/3/18
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({BeanConfig.class, UserConfig.class})
public @interface EnableFileCacheManager {
}
