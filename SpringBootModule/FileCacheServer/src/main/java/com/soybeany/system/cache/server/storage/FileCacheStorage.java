package com.soybeany.system.cache.server.storage;

import com.google.gson.Gson;
import com.soybeany.cache.v2.contract.frame.ILockSupport;
import com.soybeany.cache.v2.exception.NoCacheException;
import com.soybeany.cache.v2.model.CacheEntity;
import com.soybeany.cache.v2.model.DataCore;
import com.soybeany.cache.v2.model.DataPack;
import com.soybeany.cache.v2.model.DataParam;
import com.soybeany.cache.v2.storage.ReentrantLockSupport;
import com.soybeany.cache.v2.storage.StdStorage;
import com.soybeany.system.cache.core.dto.FileUid;
import com.soybeany.system.cache.core.security.model.FcException;
import com.soybeany.system.cache.server.model.DiscSpaceInfo;
import com.soybeany.system.cache.server.model.MetaInfo;
import com.soybeany.system.cache.server.util.InfoFileUtils;
import com.soybeany.util.file.BdFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.stream.Stream;

/**
 * todo 新建meta文件管理系统，顺带支持多文件分段管理；文件下载部分，就只需要支持简单的断点续传即可
 * todo MetaInfo增加“partialSize(long)”标识使用分段：-1或不存在，表示不启用
 *
 * @author Soybeany
 * @since 2022/8/16
 */
public class FileCacheStorage extends StdStorage<FileUid, FileCacheAccessor> implements ILockSupport<Lock, Object> {

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
    private final ILockSupport<Lock, Object> locker = new ReentrantLockSupport(desc());
    private final File cacheDir;
    private final long totalSpace;
    private final long minFreeSpaceRequired;

    public FileCacheStorage(String cacheDir, float maxUsedPercent) {
        super(Integer.MAX_VALUE, 60 * 1000);
        this.cacheDir = new File(cacheDir);
        BdFileUtils.mkDirs(this.cacheDir);
        if (maxUsedPercent < 0.1 || maxUsedPercent > 1) {
            throw new FcException("maxUsedPercent取值需在0.1~1之间");
        }
        totalSpace = this.cacheDir.getTotalSpace();
        minFreeSpaceRequired = (long) (totalSpace * (1 - maxUsedPercent));
    }

    public void start() {
        // 开启定时清理
        EXECUTOR_SERVICE.scheduleWithFixedDelay(() -> {
            try {
                deleteExpiredFiles();
            } catch (Exception e) {
                LOG.warn("清理过期文件缓存异常:" + e.getMessage());
            }
        }, 0, 1, TimeUnit.HOURS);
    }

    public void close() {
        EXECUTOR_SERVICE.shutdown();
    }

    public Map<String, MetaInfo> getMetaInfo(FileUid... fileUids) {
        Map<String, MetaInfo> result = new HashMap<>();
        Arrays.stream(fileUids).forEach(fileUid -> {
            String key = fileUid.getKey();
            getMetaInfo(getMetaFile(fileUid, key)).ifPresent(info -> result.put(key, info));
        });
        return result;
    }

    public DiscSpaceInfo getDiscSpaceInfo() {
        DiscSpaceInfo info = new DiscSpaceInfo();
        info.totalSpace = totalSpace;
        info.freeSpace = cacheDir.getFreeSpace();
        info.usableSpace = info.freeSpace - minFreeSpaceRequired;
        return info;
    }

    public synchronized void deleteExpiredFiles() {
        File[] serverDirs = Optional.ofNullable(cacheDir.listFiles()).orElseThrow(() -> new FcException("本地缓存主目录不能为文件"));
        long curTimestamp = System.currentTimeMillis();
        // 遍历server目录
        for (File serverDir : serverDirs) {
            // 按meta文件清理数据文件
            Set<String> validDataFileNames = new HashSet<>();
            for (File metaFile : Optional.ofNullable(new File(serverDir, DIR_META).listFiles()).orElseGet(() -> new File[0])) {
                Optional<MetaInfo> metaInfoOpt = getMetaInfo(metaFile);
                if (!metaInfoOpt.isPresent()) {
                    LOG.warn("文件（" + metaFile.getName() + "）读取异常，将被自动删除");
                    deleteFile(metaFile);
                    continue;
                }
                MetaInfo info = metaInfoOpt.get();
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
                LOG.warn("文件夹(" + file.getAbsolutePath() + ")访问异常");
                return false;
            }
            for (File subFile : subFiles) {
                boolean success = onDeleteFile(subFile);
                if (!success) {
                    LOG.warn("文件(" + subFile.getAbsolutePath() + ")删除异常");
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

    private static MetaInfo getMetaInfoOrThrow(File metaFile) {
        return getMetaInfo(metaFile).orElseThrow(() -> new FcException("Meta文件(" + metaFile.getName() + ")缺失"));
    }

    private static Optional<MetaInfo> getMetaInfo(File metaFile) {
        return InfoFileUtils.read(metaFile, MetaInfo.class);
    }

    @Override
    public String desc() {
        return "FILE";
    }

    @Override
    public void setNextCheckStamp(DataParam<FileUid> param, long stamp) {
        File metaFile = getMetaFile(param, getStorageKey(param));
        MetaInfo metaInfo = getMetaInfoOrThrow(metaFile);
        metaInfo.nextCheckStamp = stamp;
        writeMetaInfo(metaFile, metaInfo);
    }

    @Override
    public long getNextCheckStamp(DataParam<FileUid> param) {
        File metaFile = getMetaFile(param, getStorageKey(param));
        MetaInfo metaInfo = getMetaInfoOrThrow(metaFile);
        return Optional.ofNullable(metaInfo.nextCheckStamp).orElse(0L);
    }

    @Override
    public void onClearCache() {
        Optional.ofNullable(cacheDir.listFiles()).ifPresent(arr -> {
            for (File file : arr) {
                deleteFile(file);
            }
        });
    }

    @Override
    public int cachedDataCount() {
        return Optional.ofNullable(cacheDir.listFiles())
                .map(arr -> (int) Stream.of(arr)
                        .flatMap(serverFile -> Arrays.stream(Optional.ofNullable(new File(serverFile, DIR_DATA).list()).orElseGet(() -> new String[0])))
                        .count()
                )
                .orElse(0);
    }

    @Override
    protected CacheEntity<FileCacheAccessor> onLoadCacheEntity(DataParam<FileUid> param, String key) throws NoCacheException {
        // 读取配置
        File metaFile = getMetaFile(param, key);
        MetaInfo metaInfo = getMetaInfo(metaFile).orElseThrow(NoCacheException::new);
        DataCore<FileCacheAccessor> core;
        // 依据配置创建不同core
        if (metaInfo.norm) {
            File dataFile = getDataFile(param, metaInfo);
            // 文件不存在，或文件尺寸对不上
            if (!dataFile.exists()) {
                throw new NoCacheException();
            }
            core = DataCore.fromData(FileCacheAccessor.fromFile(metaInfo.dataInfo, dataFile));
            // 在临近失效时间时，更新缓存失效时间，同时避免频繁更新
            long currentTimeMillis = System.currentTimeMillis();
            long ttl = metaInfo.pExpireAt - currentTimeMillis;
            if (ttl > 0 && ttl < metaInfo.dataInfo.pTtl / 2) {
                metaInfo.pExpireAt = currentTimeMillis + metaInfo.dataInfo.pTtl;
                writeMetaInfo(metaFile, metaInfo);
            }
            // 如果是旧版md5，替换为新版
            if (metaInfo.dataInfo.upgradeMd5(dataFile)) {
                LOG.info("升级了" + key + "的md5");
                writeMetaInfo(metaFile, metaInfo);
            }
        } else {
            core = DataCore.fromException(getException(metaInfo));
        }
        return new CacheEntity<>(core, metaInfo.pExpireAt);
    }

    @Override
    protected CacheEntity<FileCacheAccessor> onSaveCacheEntity(DataParam<FileUid> param, String key, CacheEntity<FileCacheAccessor> entity) {
        // 先尽可能保障空间足够
        confirmDiskSpace(param);
        // 再执行后续流程
        MetaInfo metaInfo = new MetaInfo();
        long currentTimeMillis = System.currentTimeMillis();
        metaInfo.curDataFileName = key + "_" + (currentTimeMillis / 1000);
        // 正常时更新数据
        if (entity.dataCore.norm) {
            // 更新数据文件与配置文件
            FileCacheAccessor accessor = entity.dataCore.data;
            File dataFile = getDataFile(param, metaInfo);
            try {
                accessor.writeTo(dataFile);
                // 改写缓存核心
                accessor.dataInfo.checkFileIntegrity(dataFile);
                metaInfo.dataInfo = accessor.dataInfo;
                DataCore<FileCacheAccessor> newCore = DataCore.fromData(FileCacheAccessor.fromFile(metaInfo.dataInfo, dataFile));
                entity = new CacheEntity<>(newCore, entity.pExpireAt);
            } catch (Exception e) {
                deleteFile(dataFile);
                DataCore<FileCacheAccessor> newCore = DataCore.fromException(new FcException("本地缓存生成异常:" + e.getMessage()));
                entity = new CacheEntity<>(newCore, currentTimeMillis + pTtlErr);
            }
        }
        // 记录配置
        metaInfo.norm = entity.dataCore.norm;
        Optional.ofNullable(entity.dataCore.exception).ifPresent(exception -> {
            metaInfo.exceptionJson = GSON.toJson(exception);
            metaInfo.exceptionClazz = exception.getClass().getName();
        });
        metaInfo.pExpireAt = entity.pExpireAt;
        // 拷贝其它成员变量
        File metaFile = getMetaFile(param, key);
        getMetaInfo(metaFile).ifPresent(previous -> {
            metaInfo.version = previous.version;
            metaInfo.oldDataFileNames = Optional.ofNullable(previous.oldDataFileNames).orElseGet(HashSet::new);
            metaInfo.oldDataFileNames.add(previous.curDataFileName);
            metaInfo.nextCheckStamp = previous.nextCheckStamp;
        });
        // 更新配置
        writeMetaInfo(metaFile, metaInfo);
        return entity;
    }

    @Override
    protected synchronized void onRemoveCacheEntity(DataParam<FileUid> param, String key) {
        File metaFile = getMetaFile(param, key);
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
    public Lock onTryLock(String key) {
        return locker.onTryLock(key);
    }

    @Override
    public void onUnlock(Lock lock) {
        locker.onUnlock(lock);
    }

    @Override
    public Object onTryLockAll() {
        return locker.onTryLockAll();
    }

    @Override
    public void onUnlockAll(Object lock) {
        locker.onUnlockAll(lock);
    }

    // ***********************内部方法****************************

    private void confirmDiskSpace(DataParam<FileUid> param) {
        // 空间占用没达到阈值，则不处理
        long freeSpaceOld = cacheDir.getFreeSpace();
        long spaceNeeded = minFreeSpaceRequired - freeSpaceOld;
        if (spaceNeeded < 0) {
            return;
        }
        // 得到所需删除的文件列表
        long[] remainSpaceToSqueeze = {spaceNeeded};
        File[] filesToDelete = new File(cacheDir, "/" + param.value.server + DIR_DATA).listFiles(file -> {
            if (remainSpaceToSqueeze[0] < 0) {
                return false;
            }
            remainSpaceToSqueeze[0] -= file.length();
            return true;
        });
        if (null == filesToDelete) {
            throw new FcException("文件夹依旧返回null");
        }
        for (File file : filesToDelete) {
            String key = file.getName().substring(0, file.getName().lastIndexOf("_"));
            onRemoveCacheEntity(param, key);
        }
        long freeSpaceNew = cacheDir.getFreeSpace();
        LOG.warn("触发了满磁盘自动清理(" + freeSpaceOld + " -> " + freeSpaceNew + ")，" + "清理了" + filesToDelete.length + "个文件");
        if (freeSpaceNew < minFreeSpaceRequired) {
            throw new FcException("自动清理失败，达不到最低空间剩余要求(" + minFreeSpaceRequired + ")");
        }
    }

    private FcException getException(MetaInfo metaInfo) {
        Exception e;
        try {
            e = (Exception) GSON.fromJson(metaInfo.exceptionJson, Class.forName(metaInfo.exceptionClazz));
        } catch (ClassNotFoundException ex) {
            throw new FcException("无法加载指定的类:" + metaInfo.exceptionClazz);
        }
        if (!(e instanceof FcException)) {
            e = new FcException("缓存中的异常:" + e.getMessage() + "(" + e.getClass().getName() + ")");
        }
        return (FcException) e;
    }

    private File getMetaFile(DataParam<FileUid> param, String key) {
        return getMetaFile(param.value, key);
    }

    private File getMetaFile(FileUid fileUid, String key) {
        return new File(cacheDir, "/" + fileUid.server + DIR_META + "/" + key + SUFFIX_META);
    }

    private File getDataFile(DataParam<FileUid> param, MetaInfo info) {
        return new File(cacheDir, "/" + param.value.server + DIR_DATA + "/" + info.curDataFileName + SUFFIX_DATA);
    }

}
