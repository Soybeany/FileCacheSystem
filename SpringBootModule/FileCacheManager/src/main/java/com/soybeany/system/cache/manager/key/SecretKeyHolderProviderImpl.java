package com.soybeany.system.cache.manager.key;

import com.soybeany.cache.v2.contract.IDatasource;
import com.soybeany.cache.v2.core.DataManager;
import com.soybeany.cache.v2.log.ILogWriter;
import com.soybeany.cache.v2.log.StdLogger;
import com.soybeany.cache.v2.model.DataPack;
import com.soybeany.cache.v2.storage.LruMemCacheStorage;
import com.soybeany.system.cache.core.interfaces.ISecretKeyHolderProvider;
import com.soybeany.system.cache.core.interfaces.ISecretKeyRepository;
import com.soybeany.system.cache.core.model.SecretKeyInfo;
import com.soybeany.system.cache.core.token.SecretKeyHolder;
import com.soybeany.system.cache.manager.config.UserConfig;
import com.soybeany.system.cache.manager.model.CacheLogWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Soybeany
 * @date 2020/12/7
 */
@Component
public class SecretKeyHolderProviderImpl implements ISecretKeyHolderProvider {

    @Autowired
    private UserConfig userConfig;
    @Autowired
    private ISecretKeyRepository mRepository;

    private final ILogWriter mLogWriter = new CacheLogWriter();
    private final DataManager<String, WithCreateTime> mDataManager = DataManager.Builder
            .get("密钥管理器", new Datasource())
            .logger(new StdLogger<>(mLogWriter))
            .withCache(new LruMemCacheStorage.Builder<String, WithCreateTime>().build())
            .build();
    private int mOldKeyCount;
    private int mTotalKeyCount;
    private long mRenewFrequencyMillis;

    @Override
    public SecretKeyHolder.WithTtl getHolder() throws Exception {
        DataPack<WithCreateTime> pack = mDataManager.getDataPack(null);
        return new SecretKeyHolder.WithTtl(pack.getData(), pack.pTtl);
    }

    // ***********************内部方法****************************

    @PostConstruct
    private void onInit() {
        mOldKeyCount = userConfig.oldKeyCount;
        mTotalKeyCount = mOldKeyCount + userConfig.futureKeyCount + 1;
        mRenewFrequencyMillis = userConfig.renewFrequencySec * 1000L;
    }

    private boolean isTimeToRenewInfo(SecretKeyInfo keyInfo) {
        return getExpiryMillis(keyInfo.getCreateTimestamp(), mRepository.getCurrentTimestamp(), mRenewFrequencyMillis) <= 0;
    }

    private int getExpiryMillis(long createTimestamp, long curTimestamp, long validTime) {
        return (int) (createTimestamp + validTime - curTimestamp);
    }

    // ***********************内部类****************************

    private static class WithCreateTime extends SecretKeyHolder {
        /**
         * 最后一次更新的时间戳
         */
        public final long lastUpdateTimestamp;

        public WithCreateTime(String newestKey, long lastUpdateTimestamp) {
            super(newestKey);
            this.lastUpdateTimestamp = lastUpdateTimestamp;
        }
    }

    private class Datasource implements IDatasource<String, WithCreateTime> {
        @Override
        public WithCreateTime onGetData(String s) throws Exception {
            List<SecretKeyInfo> list = getInfoListFromRepository();
            try {
                int delta = mTotalKeyCount;
                // 数目不够时进行补充
                if (null == list || 0 < (delta -= list.size())) {
                    mRepository.generateNewSecretKeys(delta);
                    list = getInfoListFromRepository();
                } else {
                    SecretKeyInfo newest = list.get(list.size() - 1);
                    // 超时则重新生成信息
                    if (isTimeToRenewInfo(newest)) {
                        SecretKeyInfo old = list.get(-delta);
                        if (null != old) {
                            mRepository.removeSecretKeys(Collections.singletonList(old));
                        }
                        mRepository.generateNewSecretKeys(1);
                        list = getInfoListFromRepository();
                    }
                }
            } catch (Exception e) {
                mLogWriter.onWriteWarn(e.getMessage());
                list = getInfoListFromRepository();
            }
            return toSecretKeyHolder(list);
        }

        @Override
        public int onSetupExpiry(WithCreateTime holder) {
            return getExpiryMillis(holder.lastUpdateTimestamp, mRepository.getCurrentTimestamp(), mRenewFrequencyMillis);
        }

        private List<SecretKeyInfo> getInfoListFromRepository() throws Exception {
            List<SecretKeyInfo> list = mRepository.getSecretKeyList();
            if (null == list) {
                return null;
            }
            // 排序
            list.sort(Comparator.comparingLong(SecretKeyInfo::getCreateTimestamp));
            // 删除过多的项
            int sizeDelta = list.size() - mTotalKeyCount;
            if (sizeDelta > 0) {
                mRepository.removeSecretKeys(list.subList(0, sizeDelta));
                list = list.subList(sizeDelta, list.size());
            }
            return list;
        }

        private WithCreateTime toSecretKeyHolder(List<SecretKeyInfo> list) throws Exception {
            // 检查密钥是否存在
            if (null == list || list.size() != mTotalKeyCount) {
                throw new Exception("没有足够数量的密钥");
            }
            // 检查密钥是否已过期
            SecretKeyInfo newest = list.get(list.size() - 1);
            if (isTimeToRenewInfo(newest)) {
                throw new Exception("密钥已过期");
            }
            // 对象转换
            WithCreateTime holder = new WithCreateTime(list.get(mOldKeyCount).getKey(), list.get(list.size() - 1).getCreateTimestamp());
            for (SecretKeyInfo info : list) {
                if (null == info) {
                    throw new Exception("keyInfo不允许为null");
                }
                holder.map.put(info.getKey(), info.toSecretKey());
            }
            return holder;
        }
    }

}
