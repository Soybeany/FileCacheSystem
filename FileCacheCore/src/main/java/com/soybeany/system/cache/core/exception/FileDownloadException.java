package com.soybeany.system.cache.core.exception;

import com.soybeany.exception.BdException;
import lombok.Getter;

/**
 * @author Soybeany
 * @date 2022/2/24
 */
public class FileDownloadException extends BdException {

    @Getter
    private final int code;

    public FileDownloadException(int code, String message) {
        super(message);
        this.code = code;
    }

}
