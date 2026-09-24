package com.soybeany.system.cache.core.security.model;

import com.soybeany.cache.v2.contract.user.IDatasource;
import com.soybeany.cache.v2.core.DataManager;
import com.soybeany.cache.v2.log.ILogWriter;
import com.soybeany.cache.v2.log.StdLogger;
import com.soybeany.cache.v2.model.DataCore;
import com.soybeany.cache.v2.model.DataPack;
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
                .cacheMissHandler((param, cachedPack, fetcher) -> {
                    DataCore<SecretKeyHolder.WithExpiry> dataCore = fetcher.getData();
                    // 正常数据使用holder自身的失效时间，异常数据使用默认值(由各级缓存的有效期配置决定)
                    long pTtl = dataCore.norm ? dataCore.data.expiryMillisL : Long.MAX_VALUE;
                    return new DataPack<>(dataCore, fetcher.getProvider(), pTtl);
                })
                .logger(new StdLogger(writer))
                // 有容量限制，也有时间限制
                .withCache(new LruMemCacheStorage.Builder<String, SecretKeyHolder.WithExpiry>()
                        // 上限取holder自身的失效时间，自动续期即只续一个pTtl；异常数据仍为60秒防穿透
                        .pTtl((param, dataCore) -> dataCore.norm ? dataCore.data.expiryMillisL : 60 * 1000L)
                        .build())
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
    }
}
