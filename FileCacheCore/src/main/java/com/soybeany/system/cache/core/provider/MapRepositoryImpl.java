package com.soybeany.system.cache.core.provider;

import com.soybeany.system.cache.core.model.SecretKeyInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Soybeany
 * @date 2020/12/8
 */
public class MapRepositoryImpl implements SecretKeyProvider.Repository {

    private final Map<String, SecretKeyInfo> mMap = new HashMap<>();

    @Override
    public List<SecretKeyInfo> getSecretKeyList() {
        return new ArrayList<>(mMap.values());
    }

    @Override
    public synchronized void replaceToNewSecretKey(SecretKeyInfo old) throws Exception {
        if (null != old) {
            if (!mMap.containsKey(old.key)) {
                throw new Exception("指定的数据不存在");
            }
            // 移除旧信息
            mMap.remove(old.key);
        }
        // 添加新信息
        createAndSaveNewInfo();
    }

    @Override
    public synchronized void generateNewSecretKeys(int count) throws Exception {
        for (int i = 0; i < count; i++) {
            createAndSaveNewInfo();
        }
    }

    @Override
    public void removeSecretKeys(List<SecretKeyInfo> keyInfoList) {
        if (null == keyInfoList) {
            return;
        }
        for (SecretKeyInfo info : keyInfoList) {
            mMap.remove(info.key);
        }
    }

    @Override
    public long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }

    private void createAndSaveNewInfo() throws Exception {
        SecretKeyInfo keyInfo = SecretKeyInfo.getDefaultNew(getCurrentTimestamp());
        mMap.put(keyInfo.key, keyInfo);
    }
}
