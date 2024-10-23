package com.soybeany.system.cache.manager.controller;

import com.soybeany.system.cache.core.security.interfaces.FileCacheHttpContract.Dto;
import com.soybeany.system.cache.manager.service.IDataProvider;
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
    private IDataProvider dataProvider;

    // ********************标准API********************

    @GetMapping(GET_SECRET_KEY_LIST)
    public Dto<String> getList() {
        return Dto.norm(dataProvider.onGetSecretKeyString());
    }

}
