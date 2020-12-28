package com.soybeany.system.cache.server.config;

import com.soybeany.config.BaseDecryptConfigurer;
import org.springframework.stereotype.Component;

@Component
public class DecryptConfigurerImpl extends BaseDecryptConfigurer {
    public static final String KEY = "U2FsdGVkX1/ogCNSGmgfmL9tTsDUlRyw";

    @Override
    protected String setupKey() {
        return KEY;
    }

}