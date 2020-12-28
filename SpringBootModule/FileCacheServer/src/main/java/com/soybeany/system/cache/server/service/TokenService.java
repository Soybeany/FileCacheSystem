package com.soybeany.system.cache.server.service;

import com.soybeany.system.cache.core.consumer.SecretKeyRetriever;
import com.soybeany.system.cache.core.token.Payload;
import com.soybeany.system.cache.core.token.TokenPart;
import com.soybeany.system.cache.server.config.AppConfig;
import com.soybeany.system.cache.server.model.CacheLogWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;

/**
 * @author Soybeany
 * @date 2020/12/1
 */
public interface TokenService {

    /**
     * 获取token中的数据信息
     */
    Payload getPayload(TokenPart tokenPart) throws Exception;
}

@Service
class TokenServiceImpl implements TokenService {

    @Autowired
    private AppConfig appConfig;

    private SecretKeyRetriever keyRetriever;

    @Override
    public Payload getPayload(TokenPart tokenPart) throws Exception {
        SecretKey key = keyRetriever.getHolder().getSecretKey(tokenPart.key);
        return Payload.fromString(tokenPart.payload, key);
    }

    @PostConstruct
    void init() {
        keyRetriever = new SecretKeyRetriever(appConfig.hostProvider, new CacheLogWriter());
    }
}
