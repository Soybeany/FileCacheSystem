package com.soybeany.system.cache.manager.key;

import com.soybeany.system.cache.manager.model.SecretKeyInfo;
import com.soybeany.system.cache.manager.service.ISecretKeyRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Soybeany
 * @date 2020/12/8
 */
public class SecretKeyMapRepositoryImpl implements ISecretKeyRepository {

    private final Map<String, SecretKeyInfo> mMap = new HashMap<>();

    @Override
    public List<SecretKeyInfo> getSecretKeys() {
        return new ArrayList<>(mMap.values());
    }

    @Override
    public void addSecretKey(SecretKeyInfo info) {
        mMap.put(info.getKey(), info);
    }

    @Override
    public void deleteSecretKey(String key) {
        mMap.remove(key);
    }

    @Override
    public long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }

}
