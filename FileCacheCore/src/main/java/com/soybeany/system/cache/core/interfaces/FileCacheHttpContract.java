package com.soybeany.system.cache.core.interfaces;

import com.soybeany.util.file.BdFileUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Soybeany
 * @date 2020/12/14
 */
public interface FileCacheHttpContract {

    // ********************变量********************

    String OPT_PREFIX = "/opt";
    String CLIENT_PREFIX = "/api";

    // *****管理服务器，面向服务器，需授权*****

    String GET_SECRET_KEY_LIST = OPT_PREFIX + "/getSecretKeyList";

    // *****客户服务器，面向服务器，需授权*****

    String POST_TASK_LIST = OPT_PREFIX + "/postTaskList";

    // *****客户服务器，面向客户端*****

    String GET_FILE_PATH = CLIENT_PREFIX + "/file";

    // *****其它*****

    String AUTHORIZATION = "Authorization";

    OkHttpClient CLIENT = new OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .build();

    // ********************方法********************

    default Response getResponse(HostProvider hostProvider, String path, Map<String, String> headers) throws IOException {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        Request.Builder builder = new Request.Builder().url(hostProvider.get() + path);
        if (null != headers) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                builder.header(entry.getKey(), entry.getValue());
            }
        }
        return getResponse(builder.build());
    }

    default Response getResponse(Request request) throws IOException {
        Response response = CLIENT.newCall(request).execute();
        if (!response.isSuccessful()) {
            // 关流
            BdFileUtils.closeStream(response);
            // 抛出异常信息
            String decodedMsg = response.header("errMsg");
            String errMsg = (null != decodedMsg ? URLDecoder.decode(decodedMsg, "UTF-8") : null);
            throw new IOException("请求外部系统异常，code:" + response.code() + "，errMsg:" + errMsg);
        }
        return response;
    }

    default void sendRequest(Request request) throws IOException {
        Response response = getResponse(request);
        BdFileUtils.closeStream(response);
    }

    default ResponseBody getNonNullBody(ResponseBody body) throws IOException {
        if (null == body) {
            throw new IOException("响应主体为空");
        }
        return body;
    }

    // ********************类********************

    /**
     * 标准的dto
     */
    class Dto<T> {

        /**
         * 标识是否正常
         */
        public final boolean norm;

        public final T data;

        public final String msg;

        public static Dto<String> success() {
            return norm("success");
        }

        public static <T> Dto<T> norm(T data) {
            return new Dto<>(true, data, null);
        }

        public static <T> Dto<T> error(String msg) {
            return new Dto<>(false, null, msg);
        }

        public Dto(boolean norm, T data, String msg) {
            this.norm = norm;
            this.data = data;
            this.msg = msg;
        }
    }
}
