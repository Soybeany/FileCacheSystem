package com.soybeany.system.cache.demo.manager.service;

import com.soybeany.system.cache.core.interfaces.ISecretKeyRepository;
import com.soybeany.system.cache.core.model.SecretKeyInfo;
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
class SecretKeyServiceImpl implements ISecretKeyRepository {

    @Autowired
    private SecretKeyEntityRepository repository;

    @Override
    public List<SecretKeyInfo> getSecretKeyList() {
        List<SecretKeyInfo> list = new LinkedList<>();
        for (SecretKeyEntity entity : repository.findAll()) {
            SecretKeyInfo info = new SecretKeyInfo();
            BeanUtils.copyProperties(entity, info);
            list.add(info);
        }
        return list;
    }

    @Override
    public void generateNewSecretKeys(int count) throws Exception {
        for (int i = 0; i < count; i++) {
            SecretKeyInfo info = SecretKeyInfo.getDefaultNew(getCurrentTimestamp());
            SecretKeyEntity entity = new SecretKeyEntity();
            BeanUtils.copyProperties(info, entity);
            repository.save(entity);
        }
    }

    @Override
    public void removeSecretKeys(List<SecretKeyInfo> keyInfoList) {
        if (null == keyInfoList) {
            return;
        }
        for (SecretKeyInfo info : keyInfoList) {
            repository.findByKey(info.getKey()).ifPresent(entity -> repository.delete(entity));
        }
    }

    @Override
    public long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }

}
