package com.soybeany.system.cache.server.storage;

import com.soybeany.system.cache.server.model.DataInfo;
import com.soybeany.util.file.BdFileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.function.Supplier;

/**
 * 使用本地文件系统进行存储
 *
 * @author Soybeany
 * @since 2022/8/16
 */
public abstract class FileCacheAccessor {

    public final DataInfo dataInfo;

    protected FileCacheAccessor(DataInfo dataInfo) {
        this.dataInfo = dataInfo;
    }

    // ***********************静态构造方法****************************

    public static FileCacheAccessor fromFile(DataInfo info, File file) {
        return new Local(info, file);
    }

    public static FileCacheAccessor fromStream(DataInfo info, Supplier<InputStream> provider) {
        return new Stream(info, provider);
    }

    // ***********************成员方法****************************

    /**
     * 使用回调
     */
    public abstract void writeTo(File target) throws IOException;

    // ***********************内部类****************************

    public static class Local extends FileCacheAccessor {
        private final File file;

        public Local(DataInfo info, File file) {
            super(info);
            this.file = file;
        }

        public File file() {
            return file;
        }

        @Override
        public void writeTo(File target) throws IOException {
            try (InputStream is = Files.newInputStream(file.toPath())) {
                BdFileUtils.readWriteStream(is, target);
            }
        }
    }

    public static class Stream extends FileCacheAccessor {

        private final Supplier<InputStream> isProvider;

        public Stream(DataInfo info, Supplier<InputStream> isProvider) {
            super(info);
            this.isProvider = isProvider;
        }

        @Override
        public void writeTo(File target) throws IOException {
            try (InputStream is = isProvider.get()) {
                BdFileUtils.readWriteStream(is, target);
            }
        }
    }

}
