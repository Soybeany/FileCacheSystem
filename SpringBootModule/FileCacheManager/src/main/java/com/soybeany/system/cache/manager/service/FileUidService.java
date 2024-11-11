package com.soybeany.system.cache.manager.service;

import com.soybeany.system.cache.core.dto.FileUid;
import com.soybeany.system.cache.core.token.SecretKeyHolder;
import com.soybeany.system.cache.core.util.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@SuppressWarnings("unused")
@Service
public class FileUidService {

    @Autowired
    private SecretKeyService secretKeyService;

    public String toToken(FileUid fileUid) {
        SecretKeyHolder.WithExpiry holder = secretKeyService.getProvider().getHolder();
        return TokenUtils.toToken(holder.map::get, holder.newestKey, fileUid);
    }

}
