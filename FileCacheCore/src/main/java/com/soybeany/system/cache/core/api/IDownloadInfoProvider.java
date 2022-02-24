package com.soybeany.system.cache.core.api;

import com.soybeany.rpc.core.anno.BdRpc;
import com.soybeany.rpc.core.anno.BdRpcCache;
import com.soybeany.rpc.core.anno.BdRpcSerialize;
import com.soybeany.system.cache.core.exception.FileDownloadException;
import com.soybeany.system.cache.core.model.CacheAppInfo;

import java.util.List;
import java.util.Map;

/**
 * @author Soybeany
 * @date 2022/1/27
 */
@BdRpc
public interface IDownloadInfoProvider {

    @BdRpcCache(desc = "应用服务器信息", ttl = 30, needLog = false)
    CacheAppInfo getCacheAppInfo();

    String getFileToken(@BdRpcSerialize Map<String, List<String>> headers, @BdRpcSerialize Map<String, String[]> params) throws FileDownloadException;

}
