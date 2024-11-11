package com.soybeany.system.cache.demo.manager.controller;

import com.soybeany.system.cache.core.security.interfaces.FileCacheHttpContract.Dto;
import com.soybeany.system.cache.manager.service.SecretKeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.soybeany.system.cache.core.security.interfaces.FileCacheHttpContract.GET_SECRET_KEY_LIST;

/**
 * @author Soybeany
 * @date 2020/12/11
 */
@RestController
class ManagerApiController {

    @Autowired
    private SecretKeyService secretKeyService;

    // ********************标准API********************

    @GetMapping(GET_SECRET_KEY_LIST)
    public Dto<String> getSecretKeyList() {
        return Dto.norm(secretKeyService.getProvider().getHolderString());
    }

}
