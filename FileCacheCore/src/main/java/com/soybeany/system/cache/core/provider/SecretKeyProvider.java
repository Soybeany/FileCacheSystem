package com.soybeany.system.cache.core.provider;

import com.soybeany.cache.v2.contract.IDatasource;
import com.soybeany.cache.v2.core.DataManager;
import com.soybeany.cache.v2.log.ILogWriter;
import com.soybeany.cache.v2.log.StdLogger;
import com.soybeany.cache.v2.model.DataPack;
import com.soybeany.cache.v2.strategy.LruMemCacheStrategy;
import com.soybeany.system.cache.core.model.SecretKeyInfo;
import com.soybeany.system.cache.core.token.SecretKeyHolder;

import java.util.Comparator;
import java.util.List;

/**
 * @author Soybeany
 * @date 2020/12/7
 */
public class SecretKeyProvider {

    private final int mOldKeyCount;
    private final int mTotalKeyCount;

    private final long mRenewFrequencyMillis;
    private final Repository mRepository;
    private final ILogWriter mLogWriter;
    private final DataManager<String, WithCreateTime> mDataManager;

    /**
     * @param oldKeyCount       要保留的旧键数目
     * @param futureKeyCount    要预留的新键数目
     * @param renewFrequencySec 生成新键的频率
     */
    public SecretKeyProvider(int oldKeyCount, int futureKeyCount, int renewFrequencySec, Repository repository, ILogWriter logWriter) {
        mOldKeyCount = oldKeyCount;
        mTotalKeyCount = oldKeyCount + futureKeyCount + 1;
        mRenewFrequencyMillis = renewFrequencySec * 1000L;
        mRepository = repository;
        mLogWriter = logWriter;
        mDataManager = getNewDataManager();
    }

    public String getHolderString() throws Exception {
        DataPack<WithCreateTime> pack = mDataManager.getDataPack("secretKey列表", null);
        SecretKeyHolder.WithExpiry holder = new SecretKeyHolder.WithExpiry(pack.getData(), pack.expiryMillis);
        return SecretKeyHolder.WithExpiry.serialize(holder);
    }

    private DataManager<String, WithCreateTime> getNewDataManager() {
        return DataManager.Builder
                .get("密钥管理器", new Datasource())
                .logger(null != mLogWriter ? new StdLogger<>(mLogWriter) : null)
                .withCache(new LruMemCacheStrategy<>())
                .build();
    }

    private boolean isTimeToRenewInfo(SecretKeyInfo keyInfo) {
        return getExpiryMillis(keyInfo.createTimestamp, mRepository.getCurrentTimestamp(), mRenewFrequencyMillis) <= 0;
    }

    private int getExpiryMillis(long createTimestamp, long curTimestamp, long validTime) {
        return (int) (createTimestamp + validTime - curTimestamp);
    }

    public interface Repository {
        /**
         * 获取密钥列表
         */
        List<SecretKeyInfo> getSecretKeyList() throws Exception;

        /**
         * 将指定的旧密钥替换为新的密钥
         *
         * @param old 待替换的密钥信息，可为null
         */
        void replaceToNewSecretKey(SecretKeyInfo old) throws Exception;

        /**
         * 生成指定数目的新密钥
         */
        void generateNewSecretKeys(int count) throws Exception;

        /**
         * 移除指定的密钥
         */
        void removeSecretKeys(List<SecretKeyInfo> keyInfoList);

        /**
         * 获取当前时间戳
         */
        long getCurrentTimestamp();
    }

    private static class WithCreateTime extends SecretKeyHolder {
        /**
         * 最后一次更新的时间戳
         */
        public long lastUpdateTimestamp;
    }

    private class Datasource implements IDatasource<String, WithCreateTime> {
        @Override
        public WithCreateTime onGetData(String s) throws Exception {
            List<SecretKeyInfo> list = getInfoListFromRepository();
            try {
                int delta = mTotalKeyCount;
                // 数目不够的进行补充
                if (null == list || 0 < (delta -= list.size())) {
                    mRepository.generateNewSecretKeys(delta);
                    list = getInfoListFromRepository();
                } else {
                    SecretKeyInfo newest = list.get(list.size() - 1);
                    // 超时则重新生成信息
                    if (isTimeToRenewInfo(newest)) {
                        mRepository.replaceToNewSecretKey(list.get(-delta));
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
            list.sort(Comparator.comparingLong(o -> o.createTimestamp));
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
            WithCreateTime holder = new WithCreateTime();
            for (SecretKeyInfo info : list) {
                if (null == info) {
                    throw new Exception("keyInfo不允许为null");
                }
                holder.map.put(info.key, info.toSecretKey());
            }
            holder.newestKey = list.get(mOldKeyCount).key;
            holder.lastUpdateTimestamp = list.get(list.size() - 1).createTimestamp;
            return holder;
        }
    }

}
