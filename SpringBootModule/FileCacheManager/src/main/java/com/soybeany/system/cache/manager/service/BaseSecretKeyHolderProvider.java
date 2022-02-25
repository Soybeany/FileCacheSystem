package com.soybeany.system.cache.manager.service;

import com.soybeany.system.cache.core.api.ISecretKeyHolderProvider;
import com.soybeany.system.cache.core.model.SecretKeyHolder;
import com.soybeany.system.cache.manager.config.UserConfig;
import com.soybeany.system.cache.manager.model.SecretKeyInfo;
import lombok.RequiredArgsConstructor;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Soybeany
 * @date 2020/12/7
 */
@RequiredArgsConstructor
public abstract class BaseSecretKeyHolderProvider implements ISecretKeyHolderProvider {

    private static final int OLD_KEY_COUNT = 1;
    private static final int TOTAL_KEY_COUNT = 3;

    private final UserConfig userConfig;
    private final ISecretKeyRepository mRepository;

    private long mRenewFrequencyMillis;

    @Override
    public SecretKeyHolder getHolder() throws Exception {
        // 获取有效的SecretKeyInfo列表
        List<SecretKeyInfo> list = getValidKeyInfoList();
        // 处理成结果返回
        return toHolder(list);
    }

    // ***********************内部方法****************************

    private SecretKeyHolder toHolder(List<SecretKeyInfo> list) throws Exception {
        Map<String, SecretKey> map = new HashMap<>();
        for (SecretKeyInfo info : list) {
            map.put(info.getKey(), info.getSecretKey());
        }
        return new SecretKeyHolder(map, list.get(OLD_KEY_COUNT).getKey());
    }

    private List<SecretKeyInfo> getValidKeyInfoList() throws Exception {
        // 获取当前全部的SecretKeyInfo
        List<SecretKeyInfo> infoList = mRepository.getSecretKeys();
        // 移除失效的记录
        long currentTimestamp = mRepository.getCurrentTimestamp();
        infoList.removeIf(info -> checkAndRemoveInvalidInfo(currentTimestamp, info));
        // 删除多余的记录
        int deltaCount = TOTAL_KEY_COUNT - infoList.size();
        if (deltaCount < 0) {
            int index = 0;
            Iterator<SecretKeyInfo> it = infoList.iterator();
            while (it.hasNext()) {
                SecretKeyInfo info = it.next();
                if (index++ >= TOTAL_KEY_COUNT) {
                    mRepository.deleteSecretKey(info.getKey());
                    it.remove();
                }
            }
        }
        // 补充缺少的记录
        else if (deltaCount > 0) {
            for (int i = 0; i < deltaCount; i++) {
                SecretKeyInfo keyInfo = SecretKeyInfo.getDefaultNew(currentTimestamp);
                mRepository.addSecretKey(keyInfo);
                infoList.add(keyInfo);
            }
        }
        // 返回结果
        return infoList;
    }

    @PostConstruct
    private void onInit() {
        mRenewFrequencyMillis = userConfig.getRenewFrequencySec() * 1000L;
    }

    private boolean checkAndRemoveInvalidInfo(long currentTimestamp, SecretKeyInfo keyInfo) {
        boolean needRemove = currentTimestamp > keyInfo.getCreateTimestamp() + mRenewFrequencyMillis;
        if (needRemove) {
            mRepository.deleteSecretKey(keyInfo.getKey());
        }
        return needRemove;
    }

}
