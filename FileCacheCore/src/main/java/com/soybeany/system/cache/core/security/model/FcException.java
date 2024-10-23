package com.soybeany.system.cache.core.security.model;

import com.soybeany.exception.BdRtException;

public class FcException extends BdRtException {
    public FcException(Exception e) {
        this("非预料异常:" + e.getMessage());
    }

    public FcException(String msg) {
        super(msg);
    }
}
