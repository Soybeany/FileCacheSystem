package com.soybeany.system.cache.server.service;

import com.soybeany.download.core.TempFileInfo;
import com.soybeany.system.cache.core.dto.FileUid;
import com.soybeany.system.cache.core.security.interfaces.FileCacheHttpContract;
import com.soybeany.system.cache.server.config.AppConfig;
import com.soybeany.system.cache.server.model.DataInfo;
import com.soybeany.system.cache.server.model.ReDownloadException;
import com.soybeany.system.cache.server.model.RetryException;
import com.soybeany.system.cache.server.storage.FileCacheAccessor;
import com.soybeany.util.file.BdFileUtils;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.soybeany.download.core.BdDownloadHeaders.*;

@Service
public class DownloadAppendService implements FileCacheHttpContract {

    private static final Logger LOG = LoggerFactory.getLogger(DownloadAppendService.class);
    private static final Pattern RANGE_PATTERN = Pattern.compile(BYTES + " (\\d+)-(\\d*)/(\\d+)");
    private static final String TEMP_DIR = "temp";
    private static final String SEPARATOR = "@";
    private final Logger log = LoggerFactory.getLogger(DownloadAppendService.class);
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    @Autowired
    private AppConfig appConfig;

    private File cacheDir;

    private synchronized static void deleteTempFile(File file) {
        if (!file.exists()) {
            return;
        }
        boolean deleted = file.delete();
        if (!deleted) {
            throw new ReDownloadException("无法删除失效的断点续传临时文件“" + file.getName() + "”");
        }
    }

    public void beforeRequest(FileUid fileUid, Map<String, String> headers) {
        Optional<TempFileInfo> opt = getTempFileInfo(fileUid);
        // 若没有本地临时文件，则不作特殊处理
        if (!opt.isPresent()) {
            return;
        }
        TempFileInfo info = opt.get();
        // 启用断点续传
        headers.put(IF_RANGE, info.getETag());
        long length = info.getTempFile().length();
        headers.put(RANGE, BYTES + "=" + length + "-");
        log.info(info.getTempFile().getName() + "准备断点续传(from " + length + ")");
    }

    public Optional<FileCacheAccessor> getFileCacheAccessor(FileUid fileUid, Response response, DataInfo dataInfo) {
        // 非断点续传响应，不作处理
        if (response.code() != 206) {
            // 不为大文件，则常规处理
            if (Optional.ofNullable(dataInfo.contentLength).orElse(-1L) < appConfig.tempFileThreshold) {
                return Optional.empty();
            }
            // 为大文件，则先写到临时文件
            TempFileInfo tempFileInfo = generateTempFileinfo(fileUid, dataInfo);
            return write(response, dataInfo, tempFileInfo, false);
        }
        // 开始断点续传逻辑
        TempFileInfo tempFileInfo = getTempFileInfo(fileUid).orElseThrow(() -> new ReDownloadException("临时文件不存在"));
        try {
            // 检查能否进行断点续传
            checkAppendable(response, tempFileInfo, dataInfo);
        } catch (Exception e) {
            log.warn("断点续传检查不通过:" + e.getMessage());
            // 若有异常，则当次抛异常，且删除临时文件（下次则不会再进入此逻辑）
            deleteTempFile(tempFileInfo.getTempFile());
            throw e;
        }
        // 数据写入临时文件
        return write(response, dataInfo, tempFileInfo, true);
    }

    // ***********************内部方法****************************

    @PostConstruct
    private void onInit() {
        cacheDir = new File(appConfig.fileCacheDir, TEMP_DIR);
        BdFileUtils.mkDirs(cacheDir);
        executorService.scheduleWithFixedDelay(() -> {
            try {
                long now = System.currentTimeMillis();
                File[] filesToDelete = cacheDir.listFiles(f -> now - f.lastModified() > appConfig.tempFileRetainMills);
                if (null == filesToDelete) {
                    return;
                }
                for (File file : filesToDelete) {
                    deleteTempFile(file);
                }
            } catch (Exception e) {
                LOG.warn("清理过期临时文件异常:" + e.getMessage());
            }
        }, 0, 1, TimeUnit.HOURS);
    }

    @PreDestroy
    private void onDestroy() {
        executorService.shutdown();
    }

    private Optional<FileCacheAccessor> write(Response response, DataInfo dataInfo, TempFileInfo info, boolean append) {
        BdFileUtils.mkParentDirs(info.getTempFile());
        // 记录当次下载的内容长度
        long before = info.getTempFile().length();
        try (FileOutputStream os = new FileOutputStream(info.getTempFile(), append)) {
            BdFileUtils.readWriteStream(getNonNullBody(response.body()).byteStream(), os);
        } catch (IOException e) {
            long downloadBytes = info.getTempFile().length() - before;
            String msg = "临时文件写入异常:" + e.getMessage();
            if (downloadBytes > appConfig.tempFileThreshold) {
                throw new RetryException(msg + ";但由于本次已成功下载" + downloadBytes + "字节，即将重试");
            }
            throw new ReDownloadException(msg);
        }
        return Optional.of(new RenameFileCacheAccessor(dataInfo, info.getTempFile()));
    }

    private TempFileInfo generateTempFileinfo(FileUid fileUid, DataInfo dataInfo) {
        return new TempFileInfo(new File(cacheDir, getFileUidStr(fileUid) + SEPARATOR + dataInfo.eTag), dataInfo.eTag);
    }

    private Optional<TempFileInfo> getTempFileInfo(FileUid fileUid) {
        String fileUidStr = getFileUidStr(fileUid);
        File[] files = cacheDir.listFiles((d, n) -> n.startsWith(fileUidStr));
        if (files == null || files.length == 0) {
            return Optional.empty();
        }
        // 清理过多的缓存文件
        for (int i = 1; i < files.length; i++) {
            deleteTempFile(files[i]);
        }
        // 返回首个缓存文件
        return Optional.of(new TempFileInfo(files[0], files[0].getName().substring(fileUidStr.length() + SEPARATOR.length())));
    }

    private String getFileUidStr(FileUid fileUid) {
        String raw = FileUid.toString(fileUid);
        return raw.replaceAll("[/\\\\]", "-");
    }

    private void checkAppendable(Response response, TempFileInfo info, DataInfo dataInfo) {
        File tempFile = info.getTempFile();
        // eTag检查
        if (!Objects.equals(info.getETag(), response.header(E_TAG))) {
            throw new ReDownloadException("eTag不相等");
        }
        // 内容范围检查-响应头
        String acceptRanges = response.header(ACCEPT_RANGES);
        String contentRange = response.header(CONTENT_RANGE);
        if (!BYTES.equals(acceptRanges) || null == contentRange) {
            throw new ReDownloadException("accept-ranges不支持，或缺失content-range");
        }
        // 内容范围检查-范围格式
        Matcher matcher = RANGE_PATTERN.matcher(contentRange);
        if (!matcher.find()) {
            throw new ReDownloadException("无法解析范围格式");
        }
        // 内容范围检查-范围值
        long rangeStart = Long.parseLong(matcher.group(1));
        if (rangeStart != tempFile.length()) {
            throw new ReDownloadException("范围值不对应");
        }
        // 修正内容长度
        dataInfo.contentLength = Long.parseLong(matcher.group(3));
    }

    // ***********************内部类****************************

    private static class RenameFileCacheAccessor extends FileCacheAccessor {

        private final File file;

        protected RenameFileCacheAccessor(DataInfo dataInfo, File file) {
            super(dataInfo);
            this.file = file;
        }

        @Override
        public void writeTo(File target) {
            BdFileUtils.mkParentDirs(target);
            boolean success = file.renameTo(target);
            if (!success) {
                deleteTempFile(file);
                throw new ReDownloadException("临时文件转正异常");
            }
        }
    }
}
