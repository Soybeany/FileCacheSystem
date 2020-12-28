package com.soybeany.system.cache.core.model;

import com.soybeany.system.cache.core.interfaces.HostProvider;

/**
 * @author Soybeany
 * @date 2020/12/25
 */
public class PollingHostProvider implements HostProvider {

    private final String[] hosts;
    private int nextIndex;

    /**
     * 支持全部host在一个字符串中，使用“,”或“;”分隔
     */
    public static PollingHostProvider fromString(String hosts) {
        if (null == hosts) {
            throw new RuntimeException("hosts不允许为null");
        }
        return fromArr(hosts.split("[,;]"));
    }

    public static PollingHostProvider fromArr(String... hosts) {
        if (null == hosts) {
            throw new RuntimeException("hosts不允许为null");
        }
        String[] urls = new String[hosts.length];
        for (int i = 0; i < hosts.length; i++) {
            urls[i] = hosts[i].trim();
        }
        return new PollingHostProvider(urls);
    }

    private PollingHostProvider(String[] hosts) {
        this.hosts = hosts;
    }

    @Override
    public synchronized String get() {
        String host = hosts[nextIndex];
        nextIndex = (nextIndex + 1) % hosts.length;
        return host;
    }

}
