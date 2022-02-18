package com.soybeany.system.cache.server.service;

import com.soybeany.system.cache.core.model.Payload;
import com.soybeany.system.cache.core.model.TokenPart;

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

