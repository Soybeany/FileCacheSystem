package com.soybeany.system.cache.core.security.model;

import com.soybeany.cache.v2.contract.user.IDatasource;
import com.soybeany.cache.v2.core.DataManager;
import com.soybeany.cache.v2.log.ILogWriter;
import com.soybeany.cache.v2.log.StdLogger;
import com.soybeany.cache.v2.storage.LruMemCacheStorage;
import com.soybeany.system.cache.core.token.SecretKeyHolder;

import java.util.function.Supplier;

/**
 * @author Soybeany
 * @date 2020/12/11
 */
public class SecretKeyRetriever {

    private final Supplier<String> mSecretKeyHolderStringProvider;
    private final DataManager<String, SecretKeyHolder.WithExpiry> mDataManager;

    public SecretKeyRetriever(Supplier<String> secretKeyHolderStringProvider, ILogWriter writer) {
        mSecretKeyHolderStringProvider = secretKeyHolderStringProvider;
        mDataManager = DataManager.Builder
                .get("密钥管理器", new Datasource())
                .logger(new StdLogger(writer))
                // 有容量限制，也有时间限制
                .withCache(new LruMemCacheStorage.Builder<String, SecretKeyHolder.WithExpiry>().build())
                .enableRenewExpiredCache(true)
                .build();
    }

    public SecretKeyHolder getHolder() {
        return mDataManager.getData("keys");
    }

    private class Datasource implements IDatasource<String, SecretKeyHolder.WithExpiry> {
        @Override
        public SecretKeyHolder.WithExpiry onGetData(String key) {
            return SecretKeyHolder.WithExpiry.deserialize(mSecretKeyHolderStringProvider.get());
        }

        @Override
        public long onSetupExpiry(SecretKeyHolder.WithExpiry holder) {
            return holder.expiryMillisL;
        }
    }
}
