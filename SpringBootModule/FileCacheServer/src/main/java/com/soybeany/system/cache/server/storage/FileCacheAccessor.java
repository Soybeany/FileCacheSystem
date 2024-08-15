package com.soybeany.system.cache.server.storage;

import com.soybeany.system.cache.server.model.DataInfo;
import com.soybeany.util.file.BdFileUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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
     * 将文件内容加载为文本返回(UTF-8编码)
     */
    public String string() {
        return string(StandardCharsets.UTF_8);
    }

    /**
     * 将文件内容加载为文本返回
     */
    public String string(Charset charset) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            callback(is -> BdFileUtils.readWriteStream(is, out));
            return out.toString(charset.name());
        } catch (IOException e) {
            throw new RuntimeException("文本数据组装异常:" + e.getMessage());
        }
    }

    public File file() {
        throw new RuntimeException("当前实现不支持获取file");
    }

    /**
     * 使用回调
     */
    public abstract void callback(ICallback callback);

    // ***********************内部类****************************

    public interface ICallback {
        void onInvoke(InputStream is) throws IOException;
    }

    public static class Local extends FileCacheAccessor {
        private final File file;

        public Local(DataInfo info, File file) {
            super(info);
            this.file = file;
        }

        @Override
        public File file() {
            return file;
        }

        @Override
        public void callback(ICallback callback) {
            try (InputStream is = Files.newInputStream(file.toPath())) {
                callback.onInvoke(is);
            } catch (IOException e) {
                throw new RuntimeException("本地数据读取异常:" + e.getMessage());
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
        public void callback(ICallback callback) {
            try (InputStream is = isProvider.get()) {
                callback.onInvoke(is);
            } catch (IOException e) {
                throw new RuntimeException("本地数据读取异常:" + e.getMessage());
            }
        }
    }

}
