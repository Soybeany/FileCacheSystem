package com.soybeany.system.cache.app.impl;

import com.soybeany.mq.core.model.MqProducerMsg;
import com.soybeany.mq.producer.api.IMqMsgSender;
import com.soybeany.mq.producer.plugin.MqProducerPlugin;
import com.soybeany.rpc.consumer.api.IRpcExApiPkgProvider;
import com.soybeany.sync.client.api.IClientPlugin;
import com.soybeany.sync.client.api.IExClientPluginProvider;
import com.soybeany.system.cache.app.service.DownloadService;
import com.soybeany.system.cache.core.api.FileCacheContract;
import com.soybeany.system.cache.core.model.CacheAppInfo;
import com.soybeany.util.file.BdFileUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author Soybeany
 * @date 2022/2/23
 */
public abstract class BaseDownloadServiceImpl implements DownloadService, IMqMsgSender,
        IExClientPluginProvider, IRpcExApiPkgProvider {

    private static final String AUTHORIZATION = BdFileUtils.getUuid();

    private MqProducerPlugin producerPlugin;

    @Override
    public void onSetupApiPkgToScan(Set<String> set) {
        set.add("com.soybeany.mq.core.api");
        set.add(FileCacheContract.API_PKG_TO_SCAN);
    }

    @Override
    public void onSetupPlugins(String syncerId, List<IClientPlugin<?, ?>> plugins) {
        plugins.add(producerPlugin = new MqProducerPlugin());
    }

    @Override
    public CacheAppInfo getCacheAppInfo() {
        return new CacheAppInfo(onSetupDownloadUrl(), AUTHORIZATION);
    }

    @Override
    public boolean download(HttpServletRequest request, HttpServletResponse response, String fileToken) throws IOException {
        // 判断是否为合法用户
        if (!isLegalUser(request)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        // 下载文件
        onDownload(request, response, fileToken);
        return true;
    }

    @Override
    public <T extends Serializable> void send(String topic, MqProducerMsg<T> msg) {
        producerPlugin.send(topic, msg);
    }

    protected abstract String onSetupDownloadUrl();

    protected abstract void onDownload(HttpServletRequest request, HttpServletResponse response, String fileToken) throws IOException;

    // ***********************内部方法****************************

    private boolean isLegalUser(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(FileCacheContract.AUTHORIZATION))
                .map(AUTHORIZATION::equals)
                .orElse(false);
    }

}
