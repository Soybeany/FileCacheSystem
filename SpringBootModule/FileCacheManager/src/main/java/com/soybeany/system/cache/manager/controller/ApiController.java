package com.soybeany.system.cache.manager.controller;

import com.soybeany.system.cache.core.interfaces.FileCacheHttpContract.Dto;
import com.soybeany.system.cache.core.model.CacheTask;
import com.soybeany.system.cache.core.provider.SecretKeyProvider;
import com.soybeany.system.cache.manager.config.UserConfig;
import com.soybeany.system.cache.manager.model.CacheLogWriter;
import com.soybeany.system.cache.manager.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.List;

import static com.soybeany.system.cache.core.interfaces.FileCacheHttpContract.GET_SECRET_KEY_LIST;
import static com.soybeany.system.cache.core.interfaces.FileCacheHttpContract.POST_TASK_LIST;

/**
 * @author Soybeany
 * @date 2020/12/11
 */
@RestController
public class ApiController {

    @Autowired
    private UserConfig userConfig;
    @Autowired
    private TaskService taskService;
    @Autowired
    private SecretKeyProvider.Repository secretKeyRepository;

    private SecretKeyProvider secretKeyProvider;

    // ********************标准API********************

    @GetMapping(GET_SECRET_KEY_LIST)
    public Dto<String> getList() {
        try {
            return Dto.norm(secretKeyProvider.getHolderString());
        } catch (Exception e) {
            return Dto.error("获取失败:" + e.getMessage());
        }
    }

    @PostMapping(POST_TASK_LIST)
    Dto<String> ensure(@RequestBody List<CacheTask> tasks) {
        try {
            taskService.saveTasks(tasks);
            return Dto.success();
        } catch (Exception e) {
            return Dto.error(e.getMessage());
        }
    }

    @PostConstruct
    public void init() {
        secretKeyProvider = new SecretKeyProvider(
                userConfig.oldKeyCount,
                userConfig.futureKeyCount,
                userConfig.renewFrequencySec,
                secretKeyRepository, new CacheLogWriter()
        );
    }

}
