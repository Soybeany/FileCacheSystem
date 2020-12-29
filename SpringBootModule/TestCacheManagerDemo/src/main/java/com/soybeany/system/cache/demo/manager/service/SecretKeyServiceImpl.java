package com.soybeany.system.cache.demo.manager.service;

import com.soybeany.system.cache.core.model.SecretKeyInfo;
import com.soybeany.system.cache.core.provider.SecretKeyProvider;
import com.soybeany.system.cache.demo.manager.repository.SecretKeyEntity;
import com.soybeany.system.cache.demo.manager.repository.SecretKeyEntityRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Soybeany
 * @date 2020/12/25
 */
@Primary
@Service
class SecretKeyServiceImpl implements SecretKeyProvider.Repository {

    @Autowired
    private SecretKeyEntityRepository repository;

    @Override
    public List<SecretKeyInfo> getSecretKeyList() {
        List<SecretKeyInfo> list = new LinkedList<>();
        for (SecretKeyEntity entity : repository.findAll()) {
            list.add(toInfo(entity));
        }
        return list;
    }

    @Override
    public void replaceToNewSecretKey(SecretKeyInfo old) throws Exception {
        if (null != old) {
            removeEntity(old.key);
        }
        createAndSaveNewEntity();
    }

    @Override
    public void generateNewSecretKeys(int count) throws Exception {
        for (int i = 0; i < count; i++) {
            createAndSaveNewEntity();
        }
    }

    @Override
    public void removeSecretKeys(List<SecretKeyInfo> keyInfoList) {
        if (null == keyInfoList) {
            return;
        }
        for (SecretKeyInfo info : keyInfoList) {
            removeEntity(info.key);
        }
    }

    @Override
    public long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }

    private SecretKeyInfo toInfo(SecretKeyEntity entity) {
        SecretKeyInfo info = new SecretKeyInfo();
        BeanUtils.copyProperties(entity, info);
        return info;
    }

    private void removeEntity(String key) {
        repository.findByKey(key).ifPresent(entity -> repository.delete(entity));
    }

    private void createAndSaveNewEntity() throws Exception {
        SecretKeyInfo info = SecretKeyInfo.getDefaultNew(getCurrentTimestamp());
        SecretKeyEntity entity = new SecretKeyEntity();
        BeanUtils.copyProperties(info, entity);
        repository.save(entity);
    }
}
