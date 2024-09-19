package com.soybeany.system.cache.core.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class LogUtils {

    public static String exceptionToString(Throwable e) {
        try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
            e.printStackTrace(pw);
            return sw.toString();
        } catch (IOException ignore) {
        }
        return "异常提取失败";
    }

}
