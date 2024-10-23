package com.soybeany.system.cache.server.config;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.soybeany.system.cache.core.dto.FileUid;
import com.soybeany.system.cache.core.security.interfaces.FileCacheHttpContract;
import com.soybeany.system.cache.core.security.model.FcException;
import com.soybeany.system.cache.core.security.model.SecretKeyRetriever;
import com.soybeany.system.cache.core.util.TokenUtils;
import com.soybeany.system.cache.server.model.CacheLogWriter;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Service
public class DefaultDynamicConfigImpl implements IDynamicConfigProvider, FileCacheHttpContract {

    private static final Gson GSON = new Gson();

    @Autowired
    private AppConfig appConfig;

    private SecretKeyRetriever keyRetriever;

    @Override
    public ServerInfo getAppServer(FileUid fileUid) {
        for (ServerInfo appServer : appConfig.appServers) {
            if (appServer.name.equals(fileUid.server)) {
                return appServer;
            }
        }
        throw new FcException("没有该服务器的相关信息");
    }

    @Override
    public FileUid toFileUid(String token) {
        return TokenUtils.fromToken(key -> keyRetriever.getHolder().getSecretKey(key), token);
    }

    @Override
    public int getDownloadTimeoutSeconds() {
        return appConfig.downloadTimeoutSec;
    }

    // ***********************内部方法****************************

    @PostConstruct
    private void onInit() {
        keyRetriever = new SecretKeyRetriever(this::getSecretKeysData, new CacheLogWriter());
    }

    protected String getSecretKeysData() {
        Response response = getResponse(appConfig.managerHosts, FileCacheHttpContract.GET_SECRET_KEY_LIST, null);
        String bodyStr;
        try (ResponseBody body = response.body()) {
            bodyStr = getNonNullBody(body).string();
        } catch (IOException e) {
            throw new FcException(e);
        }
        FileCacheHttpContract.Dto<String> dto = GSON.fromJson(bodyStr, new TypeToken<FileCacheHttpContract.Dto<String>>() {
        }.getType());
        if (!dto.norm) {
            throw new FcException(dto.msg);
        }
        return dto.data;
    }

}
