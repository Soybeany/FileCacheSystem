package com.soybeany.system.cache.server.model;

import com.soybeany.exception.BdRtException;

public class RetryException extends BdRtException {
    public RetryException(String message) {
        super(message);
    }
}
