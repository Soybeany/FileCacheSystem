package com.soybeany.system.cache.server.service;

import com.soybeany.rpc.consumer.anno.BdRpcWired;
import com.soybeany.system.cache.core.api.ISecretKeyHolderProvider;
import com.soybeany.system.cache.core.model.Payload;
import com.soybeany.system.cache.core.model.TokenPart;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

/**
 * @author Soybeany
 * @date 2022/1/26
 */
@Service
public class TokenServiceImpl implements TokenService {

    @BdRpcWired
    private ISecretKeyHolderProvider provider;

    @Override
    public Payload getPayload(TokenPart tokenPart) throws Exception {
        SecretKey key = provider.getHolder().getSecretKey(tokenPart.key);
        return Payload.fromString(tokenPart.payload, key);
    }

}
