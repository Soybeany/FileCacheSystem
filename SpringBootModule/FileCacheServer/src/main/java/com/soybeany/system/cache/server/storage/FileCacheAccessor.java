package com.soybeany.system.cache.server.storage;

import com.soybeany.system.cache.server.model.DataInfo;
import com.soybeany.util.file.BdFileUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

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

    public static FileCacheAccessor fromBytes(DataInfo info, byte[] bytes) {
        return new Bytes(info, bytes);
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

    public static class Bytes extends FileCacheAccessor {

        private final byte[] bytes;

        public Bytes(DataInfo info, byte[] bytes) {
            super(info);
            this.bytes = bytes;
        }

        @Override
        public void writeTo(File target) throws IOException {
            try (ByteArrayInputStream is = new ByteArrayInputStream(bytes)) {
                BdFileUtils.readWriteStream(is, target);
            }
        }
    }

}
