package com.soybeany.system.cache.server.model;

import java.util.Set;

public class MetaInfo {
    public boolean norm;
    public int version;
    public String curDataFileName;
    public Set<String> oldDataFileNames;
    public String exceptionClazz;
    public String exceptionJson;
    public long pExpireAt;

    public DataInfo dataInfo;
}
