package com.soybeany.system.cache.core.token;

/**
 * @author Soybeany
 * @date 2020/12/1
 */
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

    public TokenPart(String server, String key, String payload) {
        this.server = server;
        this.key = key;
        this.payload = payload;
    }
}
