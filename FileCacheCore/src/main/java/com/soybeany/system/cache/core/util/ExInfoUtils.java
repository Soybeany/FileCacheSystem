package com.soybeany.system.cache.core.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public abstract class ExInfoUtils {

    public static final String HEADER_EX_INFO = "ex_info";
    private static final String ENC = "utf-8";

    public static String decodeExInfo(String encodedMsg) {
        try {
            return null != encodedMsg ? URLDecoder.decode(encodedMsg, ENC) : null;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String encodeExInfo(String decodedMsg) {
        try {
            return null != decodedMsg ? URLEncoder.encode(decodedMsg, ENC) : null;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

}
