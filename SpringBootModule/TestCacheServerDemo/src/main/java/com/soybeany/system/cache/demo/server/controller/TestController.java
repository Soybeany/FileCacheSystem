package com.soybeany.system.cache.demo.server.controller;

import com.soybeany.system.cache.core.dto.FileUid;
import com.soybeany.system.cache.server.service.CacheService;
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
    private CacheService cacheProvider;

    @GetMapping("/test")
    void test() {
        FileUid fileUid = new FileUid("app", "1234");
        cacheProvider.retrieveCache("测试", fileUid, (from, d, f) -> System.out.println("ok"));
    }
}
