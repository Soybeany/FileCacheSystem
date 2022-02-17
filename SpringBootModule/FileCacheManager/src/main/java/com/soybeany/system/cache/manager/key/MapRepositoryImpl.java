package com.soybeany.system.cache.manager.key;

import com.soybeany.system.cache.core.interfaces.ISecretKeyRepository;
import com.soybeany.system.cache.core.model.SecretKeyInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Soybeany
 * @date 2020/12/8
 */
public class MapRepositoryImpl implements ISecretKeyRepository {

    private final Map<String, SecretKeyInfo> mMap = new HashMap<>();

    @Override
    public List<SecretKeyInfo> getSecretKeyList() {
        return new ArrayList<>(mMap.values());
    }

    @Override
    public synchronized void generateNewSecretKeys(int count) throws Exception {
        for (int i = 0; i < count; i++) {
            SecretKeyInfo keyInfo = SecretKeyInfo.getDefaultNew(getCurrentTimestamp());
            mMap.put(keyInfo.getKey(), keyInfo);
        }
    }

    @Override
    public void removeSecretKeys(List<SecretKeyInfo> keyInfoList) {
        if (null == keyInfoList) {
            return;
        }
        for (SecretKeyInfo info : keyInfoList) {
            mMap.remove(info.getKey());
        }
    }

    @Override
    public long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }

}
