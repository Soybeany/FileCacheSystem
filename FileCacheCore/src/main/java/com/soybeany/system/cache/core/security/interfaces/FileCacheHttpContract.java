package com.soybeany.system.cache.core.security.interfaces;

import com.soybeany.system.cache.core.security.model.FcException;
import com.soybeany.util.file.BdFileUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
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

    // *****客户服务器，面向客户端*****

    String GET_FILE_PATH = CLIENT_PREFIX + "/file";

    // *****其它*****

    String HEADER_AUTHORIZATION = "Authorization";
    String HEADER_ERR_MSG = "errMsg";
    String HEADER_EX_INFO = "ex_info";

    OkHttpClient CLIENT = getNewClient(5);

    // ********************方法********************

    static OkHttpClient getNewClient(int timeoutSec) {
        return new OkHttpClient.Builder()
                .connectTimeout(timeoutSec, TimeUnit.SECONDS)
                .readTimeout(timeoutSec, TimeUnit.SECONDS)
                .writeTimeout(timeoutSec, TimeUnit.SECONDS)
                .build();
    }

    @SuppressWarnings("unused")
    static void safeWrap(HttpServletResponse response, ICallback callback) throws IOException {
        try {
            callback.onInvoke();
        } catch (Exception e) {
            callback.onException(e);
            if (response.isCommitted()) {
                return;
            }
            response.reset();
            response.setHeader(HEADER_ERR_MSG, URLEncoder.encode(e.getMessage(), "UTF-8"));
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    static String decodeExInfo(String encodedMsg) {
        try {
            return null != encodedMsg ? URLDecoder.decode(encodedMsg, "utf-8") : null;
        } catch (UnsupportedEncodingException e) {
            throw new FcException(e);
        }
    }

    static String encodeExInfo(String decodedMsg) {
        try {
            return null != decodedMsg ? URLEncoder.encode(decodedMsg, "utf-8") : null;
        } catch (UnsupportedEncodingException e) {
            throw new FcException(e);
        }
    }

    default Response getResponse(HostProvider hostProvider, String path, Map<String, String> headers) {
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

    default Response getResponse(Request request) {
        try {
            Response response = getClient().newCall(request).execute();
            if (!response.isSuccessful()) {
                // 关流
                BdFileUtils.closeStream(response);
                // 抛出异常信息
                String decodedMsg = response.header(HEADER_ERR_MSG);
                String errMsg = (null != decodedMsg ? URLDecoder.decode(decodedMsg, "UTF-8") : null);
                throw new FcException("请求外部系统异常，code:" + response.code() + "，errMsg:" + errMsg);
            }
            return response;
        } catch (IOException e) {
            throw new FcException(e);
        }
    }

    default ResponseBody getNonNullBody(ResponseBody body) {
        if (null == body) {
            throw new FcException("响应主体为空");
        }
        return body;
    }

    default OkHttpClient getClient() {
        return CLIENT;
    }

    // ********************类********************

    interface ICallback {
        void onInvoke() throws IOException;

        void onException(Exception e);
    }

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

        @SuppressWarnings("unused")
        public static Dto<String> success() {
            return norm("success");
        }

        public static <T> Dto<T> norm(T data) {
            return new Dto<>(true, data, null);
        }

        @SuppressWarnings("unused")
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
