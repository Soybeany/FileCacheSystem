package com.soybeany.system.cache.demo.server.controller;

import com.soybeany.system.cache.core.dto.FileUid;
import com.soybeany.system.cache.server.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Soybeany
 * @date 2020/11/30
 */
@RestController
class TestController {

    @Autowired
    private ManageService cacheProvider;

    @GetMapping("/test")
    void test() {
        FileUid fileUid = new FileUid("app", "1234");
        cacheProvider.retrieveCache(fileUid, (d, f) -> {
            System.out.println("ok");
            return null;
        });
    }
}
