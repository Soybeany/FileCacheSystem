package com.soybeany.system.cache.core.util;

import com.soybeany.cache.v2.exception.BdCacheException;
import com.soybeany.system.cache.core.security.model.FcException;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class LogUtils {

    public static String exceptionToString(Throwable e) {
        // 已知异常直接提取文本
        if (e instanceof FcException || e instanceof BdCacheException) {
            return e.getMessage();
        }
        // 未知异常打印堆栈
        try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
            e.printStackTrace(pw);
            return sw.toString();
        } catch (IOException ignore) {
        }
        // 提取失败则输出固定文本
        return "异常提取失败";
    }

}
