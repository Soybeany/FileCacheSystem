package com.soybeany.system.cache.core.model;

import lombok.RequiredArgsConstructor;

/**
 * @author Soybeany
 * @date 2020/12/1
 */
@RequiredArgsConstructor
public class TokenPart {

    private static final String SEPARATOR = "-";

    public final String server;
    public final String key;
    public final String payload;

    public static String toToken(TokenPart part) {
        return part.server + SEPARATOR + part.key + SEPARATOR + part.payload;
    }

    public static TokenPart fromToken(String token) throws Exception {
        String[] parts = token.split(SEPARATOR);
        if (parts.length != 3) {
            throw new Exception("token格式不正确");
        }
        return new TokenPart(parts[0], parts[1], parts[2]);
    }

}
