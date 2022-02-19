package com.soybeany.system.cache.demo.app.service;

import com.soybeany.system.cache.core.api.FileCacheContract;
import com.soybeany.system.cache.core.api.ICacheAppInfoProvider;
import com.soybeany.system.cache.core.model.CacheAppInfo;
import com.soybeany.util.file.BdFileUtils;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * @author Soybeany
 * @date 2022/2/19
 */
@Service
public class AppInfoProvideServiceImpl implements ICacheAppInfoProvider, AuthorizationVerifyService {

    private static final String AUTHORIZATION = BdFileUtils.getUuid();

    @Override
    public CacheAppInfo getInfo() {
        return new CacheAppInfo(
                "http://localhost:8183/api/download",
                AUTHORIZATION
        );
    }

    @Override
    public boolean isLegalUser(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(FileCacheContract.AUTHORIZATION))
                .map(AUTHORIZATION::equals)
                .orElse(false);
    }
}
