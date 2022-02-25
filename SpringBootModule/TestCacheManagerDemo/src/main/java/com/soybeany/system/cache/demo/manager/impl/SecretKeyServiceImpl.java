package com.soybeany.system.cache.demo.manager.impl;

import com.soybeany.system.cache.demo.manager.model.SecretKeyEntity;
import com.soybeany.system.cache.demo.manager.repository.SecretKeyEntityRepository;
import com.soybeany.system.cache.manager.model.SecretKeyInfo;
import com.soybeany.system.cache.manager.service.ISecretKeyRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Soybeany
 * @date 2020/12/25
 */
@Service
class SecretKeyServiceImpl implements ISecretKeyRepository {

    @Autowired
    private SecretKeyEntityRepository repository;

    @Override
    public List<SecretKeyInfo> getSecretKeys() {
        List<SecretKeyInfo> list = new ArrayList<>();
        for (SecretKeyEntity entity : repository.findAll()) {
            list.add(new SecretKeyInfo(entity.getKey(), entity.getSecretKeyJson(), entity.getCreateTimestamp()));
        }
        return list;
    }

    @Override
    public synchronized void addSecretKey(SecretKeyInfo info) {
        SecretKeyEntity entity = new SecretKeyEntity();
        BeanUtils.copyProperties(info, entity);
        repository.save(entity);
    }

    @Override
    public synchronized void deleteSecretKey(String key) {
        repository.findByKey(key).ifPresent(entity -> repository.delete(entity));
    }

    @Override
    public long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }

}
