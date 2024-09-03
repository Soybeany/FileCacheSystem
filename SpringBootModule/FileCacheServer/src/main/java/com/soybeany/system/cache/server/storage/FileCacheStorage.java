package com.soybeany.system.cache.server.storage;

import com.google.gson.Gson;
import com.soybeany.cache.v2.exception.NoCacheException;
import com.soybeany.cache.v2.model.CacheEntity;
import com.soybeany.cache.v2.model.DataContext;
import com.soybeany.cache.v2.model.DataCore;
import com.soybeany.cache.v2.model.DataPack;
import com.soybeany.cache.v2.storage.StdStorage;
import com.soybeany.system.cache.core.dto.FileUid;
import com.soybeany.system.cache.server.model.DataInfo;
import com.soybeany.system.cache.server.util.InfoFileUtils;
import com.soybeany.util.file.BdFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * @author Soybeany
 * @since 2022/8/16
 */
public class FileCacheStorage extends StdStorage<FileUid, FileCacheAccessor> {

    private static final Logger LOG = LoggerFactory.getLogger(FileCacheStorage.class);
    private static final Gson GSON = new Gson();
    private static final ScheduledExecutorService EXECUTOR_SERVICE = Executors.newScheduledThreadPool(1);

    private static final String SUFFIX_META = ".meta";
    private static final String SUFFIX_DATA = ".data";

    private static final String DIR_META = "/meta";
    private static final String DIR_DATA = "/data";

    /**
     * 全部缓存的根目录
     */
    private final File cacheDir;

    public FileCacheStorage(String cacheDir) {
        super(Integer.MAX_VALUE, 60 * 1000);
        this.cacheDir = new File(cacheDir);
    }

    public void start() {
        // 开启定时清理
        EXECUTOR_SERVICE.scheduleWithFixedDelay(() -> {
            try {
                deleteExpiredFiles();
            } catch (Exception e) {
                LOG.warn("清理过期文件缓存异常:" + e.getMessage());
            }
        }, 1, 1, TimeUnit.HOURS);
    }

    public void close() {
        EXECUTOR_SERVICE.shutdown();
    }

    public void deleteExpiredFiles() {
        File[] serverDirs = Optional.ofNullable(cacheDir.listFiles()).orElseThrow(() -> new RuntimeException("本地缓存主目录不能为文件"));
        long curTimestamp = System.currentTimeMillis();
        // 遍历server目录
        for (File serverDir : serverDirs) {
            // 按meta文件清理数据文件
            Set<String> validDataFileNames = new HashSet<>();
            for (File metaFile : Optional.ofNullable(new File(serverDir, DIR_META).listFiles()).orElseGet(() -> new File[0])) {
                MetaInfo info = getMetaInfo(metaFile).orElseThrow(() -> new RuntimeException("找不到metaInfo文件（" + metaFile.getName() + "）"));
                boolean isCurDataExpired = curTimestamp > info.pExpireAt;
                deleteDataAndMetaFiles(metaFile, info, isCurDataExpired);
                if (!isCurDataExpired) {
                    validDataFileNames.add(info.curDataFileName + SUFFIX_DATA);
                }
            }
            // 清理残余数据文件
            for (File dataFile : Optional.ofNullable(new File(serverDir, DIR_DATA).listFiles()).orElseGet(() -> new File[0])) {
                if (!validDataFileNames.contains(dataFile.getName())) {
                    deleteFile(dataFile);
                }
            }
        }
    }

    private static void deleteDataAndMetaFiles(File metaFile, MetaInfo info, boolean containCur) {
        Set<String> toDelete = Optional.ofNullable(info.oldDataFileNames).orElseGet(() -> info.oldDataFileNames = new HashSet<>());
        // 当前文件按需添加到待删除列表
        if (containCur) {
            toDelete.add(info.curDataFileName);
        }
        // 若没有需要删除的文件，直接返回
        if (toDelete.isEmpty()) {
            return;
        }
        // 删除旧文件
        boolean allDeleted = true;
        Iterator<String> iterator = toDelete.iterator();
        File cacheTypeDir = new File(metaFile.getParentFile().getParentFile(), DIR_DATA);
        while (iterator.hasNext()) {
            boolean deleted = deleteFile(new File(cacheTypeDir, iterator.next() + SUFFIX_DATA));
            if (deleted) {
                iterator.remove();
            }
            allDeleted = allDeleted && deleted;
        }
        if (containCur && allDeleted) {
            LOG.info(metaFile.getName() + "的全部数据文件已清理成功");
            deleteFile(metaFile);
        } else {
            LOG.warn(metaFile.getName() + "的部分数据文件未清理成功");
            writeMetaInfo(metaFile, info);
        }
    }

    private static boolean deleteFile(File file) {
        if (!file.exists()) {
            return true;
        }
        return onDeleteFile(file);
    }

    private static boolean onDeleteFile(File file) {
        // 如果file为目录，递归删除
        if (file.isDirectory()) {
            File[] subFiles = file.listFiles();
            if (null == subFiles) {
                throw new RuntimeException("文件夹依旧返回null");
            }
            for (File subFile : subFiles) {
                boolean success = onDeleteFile(subFile);
                if (!success) {
                    LOG.warn("文件(" + file.getAbsolutePath() + ")删除异常");
                    return false;
                }
            }
        }
        // 直接删除
        return file.delete();
    }

    private static void writeMetaInfo(File metaFile, MetaInfo info) {
        info.version++;
        InfoFileUtils.write(metaFile, info);
    }

    private static Optional<MetaInfo> getMetaInfo(File metaFile) {
        return InfoFileUtils.read(metaFile, MetaInfo.class);
    }

    @Override
    public String desc() {
        return "FILE";
    }

    @Override
    public boolean needDoubleCheck() {
        return false;
    }

    @Override
    protected CacheEntity<FileCacheAccessor> onLoadCacheEntity(DataContext<FileUid> context, String key) throws NoCacheException {
        key = preTreatKey(key);
        // 读取配置
        File metaFile = getMetaFile(context, key);
        MetaInfo metaInfo = getMetaInfo(metaFile).orElseThrow(NoCacheException::new);
        DataCore<FileCacheAccessor> core;
        // 依据配置创建不同core
        if (metaInfo.norm) {
            File dataFile = getDataFile(context, metaInfo);
            // 文件不存在，或文件尺寸对不上
            if (!dataFile.exists()) {
                throw new NoCacheException();
            }
            core = DataCore.fromData(FileCacheAccessor.fromFile(metaInfo.dataInfo, dataFile));
            // 在临近失效时间时，更新缓存失效时间，同时避免频繁更新
            long currentTimeMillis = System.currentTimeMillis();
            if (metaInfo.pExpireAt - currentTimeMillis < metaInfo.dataInfo.pTtl / 2) {
                metaInfo.pExpireAt = currentTimeMillis + metaInfo.dataInfo.pTtl;
                writeMetaInfo(metaFile, metaInfo);
            }
        } else {
            core = DataCore.fromException(getException(metaInfo));
        }
        return new CacheEntity<>(core, metaInfo.pExpireAt);
    }

    @Override
    protected CacheEntity<FileCacheAccessor> onSaveCacheEntity(DataContext<FileUid> context, String key, CacheEntity<FileCacheAccessor> entity) {
        MetaInfo metaInfo = new MetaInfo();
        long currentTimeMillis = System.currentTimeMillis();
        metaInfo.curDataFileName = key + "_" + (currentTimeMillis / 1000);
        // 正常时更新数据
        if (entity.dataCore.norm) {
            // 更新数据文件与配置文件
            FileCacheAccessor data = entity.dataCore.data;
            File dataFile = getDataFile(context, metaInfo);
            try {
                data.callback(is -> BdFileUtils.readWriteStream(is, dataFile));
                // 改写缓存核心
                if (data.dataInfo.isFileComplete(dataFile)) {
                    metaInfo.dataInfo = data.dataInfo;
                    DataCore<FileCacheAccessor> newCore = DataCore.fromData(FileCacheAccessor.fromFile(metaInfo.dataInfo, dataFile));
                    entity = new CacheEntity<>(newCore, entity.pExpireAt);
                } else {
                    throw new IOException("文件大小不正确，可能下载不完整");
                }
            } catch (IOException e) {
                deleteFile(dataFile);
                DataCore<FileCacheAccessor> newCore = DataCore.fromException(new RuntimeException("本地缓存生成异常:" + e.getMessage()));
                entity = new CacheEntity<>(newCore, currentTimeMillis + pTtlErr);
            }
        }
        // 记录配置
        key = preTreatKey(key);
        metaInfo.norm = entity.dataCore.norm;
        Optional.ofNullable(entity.dataCore.exception).ifPresent(exception -> {
            metaInfo.exceptionJson = GSON.toJson(exception);
            metaInfo.exceptionClazz = exception.getClass().getName();
        });
        metaInfo.pExpireAt = entity.pExpireAt;
        // 拷贝未完成删除的历史文件
        File metaFile = getMetaFile(context, key);
        getMetaInfo(metaFile).ifPresent(previous -> {
            metaInfo.version = previous.version;
            metaInfo.oldDataFileNames = Optional.ofNullable(previous.oldDataFileNames).orElseGet(HashSet::new);
            metaInfo.oldDataFileNames.add(previous.curDataFileName);
        });
        // 更新配置
        writeMetaInfo(metaFile, metaInfo);
        return entity;
    }

    @Override
    protected void onRemoveCacheEntity(DataContext<FileUid> context, String key) {
        key = preTreatKey(key);
        File metaFile = getMetaFile(context, key);
        getMetaInfo(metaFile).ifPresent(info -> deleteDataAndMetaFiles(metaFile, info, true));
    }

    @Override
    protected DataPack<FileCacheAccessor> onRewriteCacheData(CacheEntity<FileCacheAccessor> cacheEntity, CacheEntity<FileCacheAccessor> newCacheEntity, DataPack<FileCacheAccessor> data) {
        return newCacheEntity == cacheEntity ? data : CacheEntity.toDataPack(newCacheEntity, data.provider, this.onGetCurTimestamp());
    }

    @Override
    protected long onGetCurTimestamp() {
        return System.currentTimeMillis();
    }

    @Override
    public void onClearCache(String storageId) {
        Optional.ofNullable(cacheDir.listFiles()).ifPresent(arr -> {
            for (File file : arr) {
                deleteFile(file);
            }
        });
    }

    @Override
    public int cachedDataCount(String storageId) {
        return Optional.ofNullable(cacheDir.listFiles())
                .map(arr -> (int) Stream.of(arr)
                        .flatMap(serverFile -> Arrays.stream(Optional.ofNullable(new File(serverFile, DIR_DATA).list()).orElseGet(() -> new String[0])))
                        .count()
                )
                .orElse(0);
    }

    // ***********************内部方法****************************

    private RuntimeException getException(MetaInfo metaInfo) {
        Exception e;
        try {
            e = (Exception) GSON.fromJson(metaInfo.exceptionJson, Class.forName(metaInfo.exceptionClazz));
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException("无法加载指定的类:" + metaInfo.exceptionClazz);
        }
        if (!(e instanceof RuntimeException)) {
            e = new RuntimeException(e);
        }
        return (RuntimeException) e;
    }

    private String preTreatKey(String key) {
        return key.replaceAll("[/\\\\]", "-");
    }

    private File getMetaFile(DataContext<FileUid> context, String key) {
        return new File(cacheDir, "/" + context.param.param.server + DIR_META + "/" + key + SUFFIX_META);
    }

    private File getDataFile(DataContext<FileUid> context, MetaInfo info) {
        return new File(cacheDir, "/" + context.param.param.server + DIR_DATA + "/" + info.curDataFileName + SUFFIX_DATA);
    }

    private static class MetaInfo {
        public boolean norm;
        public int version;
        public String curDataFileName;
        public Set<String> oldDataFileNames;
        public String exceptionClazz;
        public String exceptionJson;
        public long pExpireAt;

        public DataInfo dataInfo;
    }
}
