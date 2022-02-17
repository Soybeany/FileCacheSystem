package com.soybeany.system.cache.core.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;

/**
 * @author Soybeany
 * @date 2020/12/17
 */
public class CacheCoreTimeUtils {

    public static LocalDateTime toLocalDateTime(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    public static Date toDate(LocalDateTime time) {
        return Date.from(time.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static long toMillis(LocalDateTime time) {
        return time.toInstant(ZoneOffset.of("+8")).toEpochMilli();
    }

}
