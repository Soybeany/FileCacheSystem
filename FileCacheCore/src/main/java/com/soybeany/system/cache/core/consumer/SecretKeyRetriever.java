package com.soybeany.system.cache.core.consumer;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.soybeany.cache.v2.contract.IDatasource;
import com.soybeany.cache.v2.core.DataManager;
import com.soybeany.cache.v2.log.ILogWriter;
import com.soybeany.cache.v2.log.StdLogger;
import com.soybeany.cache.v2.strategy.LruMemCacheStrategy;
import com.soybeany.system.cache.core.interfaces.FileCacheHttpContract;
import com.soybeany.system.cache.core.interfaces.HostProvider;
import com.soybeany.system.cache.core.token.SecretKeyHolder;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * @author Soybeany
 * @date 2020/12/11
 */
public class SecretKeyRetriever {

    private static final Gson GSON = new Gson();

    private final HostProvider mHostProvider;
    private final DataManager<String, SecretKeyHolder.WithExpiry> mDataManager;

    public SecretKeyRetriever(HostProvider hostProvider, ILogWriter writer) {
        mHostProvider = hostProvider;
        mDataManager = DataManager.Builder
                .get("密钥管理器", new Datasource())
                .logger(new StdLogger<>(writer))
                // 有容量限制，也有时间限制
                .withCache(new LruMemCacheStrategy<String, SecretKeyHolder.WithExpiry>().capacity(5))
                .build();
    }

    public SecretKeyHolder getHolder() throws Exception {
        return mDataManager.getData("SecretKeyHolder", null);
    }

    private class Datasource implements IDatasource<String, SecretKeyHolder.WithExpiry>, FileCacheHttpContract {
        @Override
        public SecretKeyHolder.WithExpiry onGetData(String key) throws Exception {
            Response response = getResponse(mHostProvider, FileCacheHttpContract.GET_SECRET_KEY_LIST, null);
            String bodyStr;
            try (ResponseBody body = response.body()) {
                bodyStr = getNonNullBody(body).string();
            }
            Dto<String> dto = GSON.fromJson(bodyStr, new TypeToken<Dto<String>>() {
            }.getType());
            if (!dto.norm) {
                throw new RuntimeException(dto.msg);
            }
            return SecretKeyHolder.WithExpiry.deserialize(dto.data);
        }

        @Override
        public int onSetupExpiry(SecretKeyHolder.WithExpiry holder) {
            return holder.expiryMillis;
        }
    }
}
