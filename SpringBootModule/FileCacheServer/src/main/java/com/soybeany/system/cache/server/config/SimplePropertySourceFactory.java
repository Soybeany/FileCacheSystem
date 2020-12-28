package com.soybeany.system.cache.server.config;

import com.soybeany.config.BDCipherUtils;
import com.soybeany.config.CompositePropertySourceFactory;

import java.util.Properties;

/**
 * @author Soybeany
 * @date 2020/11/30
 */
public class SimplePropertySourceFactory extends CompositePropertySourceFactory {
    @Override
    protected void onLoadYml(Properties properties) {
        super.onLoadYml(properties);
        for (Object key : properties.keySet()) {
            String property = properties.getProperty((String) key);
            try {
                if (BDCipherUtils.isWithProtocol(property)) {
                    String newProperty = BDCipherUtils.decryptIfWithProtocol(DecryptConfigurerImpl.KEY, property);
                    properties.put(key, newProperty);
                }
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        }
    }
}
