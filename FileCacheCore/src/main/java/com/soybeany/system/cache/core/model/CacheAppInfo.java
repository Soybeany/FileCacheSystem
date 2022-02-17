package com.soybeany.system.cache.core.model;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Soybeany
 * @date 2022/1/27
 */
@Data
@Accessors(fluent = true, chain = true)
public class CacheAppInfo {

    private final String downloadUrl;
    private String fileTokenParamName = "fileToken";
    private final String authorization;

    public String getCompleteUrl(String fileToken) {
        return downloadUrl + "?" + fileTokenParamName + "=" + fileToken;
    }

}
