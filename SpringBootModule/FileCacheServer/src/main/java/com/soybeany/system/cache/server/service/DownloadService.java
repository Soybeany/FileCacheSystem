package com.soybeany.system.cache.server.service;

import com.soybeany.system.cache.core.interfaces.FileCacheHttpContract;
import com.soybeany.system.cache.core.model.FileUid;
import com.soybeany.system.cache.core.model.PollingHostProvider;
import com.soybeany.system.cache.server.config.AppConfig;
import com.soybeany.system.cache.server.config.ServerInfo;
import com.soybeany.system.cache.server.model.CacheInfo;
import com.soybeany.system.cache.server.model.CacheInfoWithExpiry;
import com.soybeany.system.cache.server.repository.TempFileInfo;
import com.soybeany.system.cache.server.repository.TempFileRepository;
import com.soybeany.system.cache.server.util.SaveUtils;
import com.soybeany.util.file.BdFileUtils;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Soybeany
 * @date 2020/12/1
 */
public interface DownloadService {

    CacheInfoWithExpiry downloadFile(FileUid fileUid, File localFile) throws IOException;

}

@Service
class DownloadServiceImpl implements DownloadService, FileCacheHttpContract {

    private static final Logger LOG = LoggerFactory.getLogger(DownloadServiceImpl.class);
    private static final Pattern RANGE_PATTERN = Pattern.compile("bytes (\\d+)-(\\d*)/(\\d+)");

    @Autowired
    private AppConfig appConfig;
    @Autowired
    private ConfigService configService;
    @Autowired
    private TempFileRepository tempFileRepository;

    private OkHttpClient client;

    @Override
    public OkHttpClient getClient() {
        return client;
    }

    @Override
    public CacheInfoWithExpiry downloadFile(FileUid fileUid, File localFile) throws IOException {
        // 读取可断点续传的记录
        TempFileInfo tempFileInfo = tempFileRepository.findByFileUid(FileUid.toString(fileUid));
        File tempFile = getTempFile(tempFileInfo);
        // 将远端文件写入到本地
        FileRetriever fileRetriever = new FileRetriever(fileUid, tempFileInfo, tempFile);
        CacheInfoWithExpiry contentInfo = fileRetriever.retrieveFile();
        // 校验文件
        boolean complete = CacheInfo.isFileComplete(contentInfo.getContentLength(), tempFile);
        if (!complete) {
            throw new RuntimeException("“" + fileUid.fileToken + "”文件大小校验失败，文件可能下载不完整");
        }
        // 将临时文件取代正式文件
        BdFileUtils.mkParentDirs(localFile);
        if (localFile.exists()) {
            boolean deleted = localFile.delete();
            LOG.info("“" + fileUid.fileToken + "”旧文件删除" + (deleted ? "成功" : "失败"));
        }
        boolean renamed = tempFile.renameTo(localFile);
        String msg = "“" + fileUid.fileToken + "”下载成功，文件替换" + (renamed ? "成功" : "失败");
        LOG.info(msg);
        if (!renamed) {
            throw new RuntimeException(msg);
        }
        return contentInfo;
    }

    @PostConstruct
    private void onInit() {
        client = FileCacheHttpContract.getNewClient(appConfig.downloadTimeoutSec);
    }

    private File getTempFile(TempFileInfo tempFileInfo) {
        if (null == tempFileInfo) {
            return getNewTempFile();
        }
        return new File(configService.getTempDir(), tempFileInfo.tempFileName);
    }

    private File getNewTempFile() {
        return new File(configService.getTempDir(), UUID.randomUUID().toString());
    }

    private class FileRetriever {

        private final ServerInfo info;
        private final String fileToken;
        private final String fileUid;
        private final TempFileInfo tempFileInfo;
        private final File tempFile;

        public FileRetriever(FileUid fileUid, TempFileInfo tempFileInfo, File tempFile) {
            this.info = configService.getServerInfo(fileUid.server);
            this.fileToken = fileUid.fileToken;
            this.fileUid = FileUid.toString(fileUid);
            this.tempFileInfo = tempFileInfo;
            this.tempFile = tempFile;
        }

        public CacheInfoWithExpiry retrieveFile() throws IOException {
            Response response;
            try {
                response = getResponse(PollingHostProvider.fromArr(info.fileDownloadUrl), fileToken, onSetupRequestHeaders());
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage());
            }
            CacheInfoWithExpiry contentInfo = CacheInfoWithExpiry.fromResponse(response);
            try (ResponseBody body = response.body()) {
                onHandleStream(response, getNonNullBody(body).byteStream(), contentInfo);
            }
            return contentInfo;
        }

        private Map<String, String> onSetupRequestHeaders() {
            Map<String, String> headers = new HashMap<>();
            headers.put(FileCacheHttpContract.AUTHORIZATION, info.authorization);
            if (null != tempFileInfo && null != tempFileInfo.eTag && tempFile.exists()) {
                headers.put("If-Range", tempFileInfo.eTag);
                headers.put("Range", "bytes=" + getTempFileLength() + "-");
            }
            return headers;
        }

        private void onHandleStream(Response response, InputStream is, CacheInfoWithExpiry info) {
            // 校验断点续传情况
            checkResponse(response, info);
            // 将文件写入到临时文件
            BdFileUtils.mkParentDirs(tempFile);
            try (FileOutputStream os = new FileOutputStream(tempFile, true)) {
                BdFileUtils.readWriteStream(is, os);
            } catch (IOException e) {
                LOG.warn("“" + fileUid + "”下载异常");
                onHandleDownloadException(info, response, e);
            }
            deleteTempFileInfoIfExist();
        }

        private void onHandleDownloadException(CacheInfoWithExpiry info, Response response, IOException e) {
            if ("bytes".equals(response.header("Accept-Ranges"))
                    && null != info.getEtag()) {
                saveTempFileInfo(info);
            } else {
                deleteTempFileInfoAndTempFile("不支持断点续传", false);
            }
            throw new RuntimeException(e.getMessage());
        }

        private void saveTempFileInfo(CacheInfoWithExpiry info) {
            TempFileInfo tempFileInfo = this.tempFileInfo;
            if (null == tempFileInfo) {
                tempFileInfo = new TempFileInfo();
                tempFileInfo.fileUid = fileUid;
                tempFileInfo.eTag = info.getEtag();
                tempFileInfo.tempFileName = tempFile.getName();
            }
            tempFileInfo.downloadedLength = getTempFileLength();
            tempFileInfo.totalLength = info.getContentLength();
            SaveUtils.syncSave(tempFileRepository, tempFileInfo);
        }

        private void deleteTempFileInfoIfExist() {
            if (null == tempFileInfo) {
                return;
            }
            tempFileRepository.delete(tempFileInfo);
        }

        private void checkResponse(Response response, CacheInfoWithExpiry info) {
            // 状态码检查
            if (response.code() != 206) {
                deleteTempFileInfoAndTempFile("不是断点续传的响应", false);
                return;
            }
            // 内容范围检查-响应头
            String contentRange = response.header("Content-Range");
            if (null == contentRange) {
                deleteTempFileInfoAndTempFile("缺失content-range", true);
                return;
            }
            // 内容范围检查-范围格式
            Matcher matcher = RANGE_PATTERN.matcher(contentRange);
            if (!matcher.find()) {
                deleteTempFileInfoAndTempFile("无法解析范围格式", true);
                return;
            }
            // 内容范围检查-范围值
            long rangeStart = Long.parseLong(matcher.group(1));
            if (rangeStart != getTempFileLength()) {
                deleteTempFileInfoAndTempFile("范围值不对应", true);
                return;
            }
            info.setContentLength(matcher.group(3));
            LOG.info(fileUid + "支持从“" + rangeStart + "”开始断点续传");
        }

        private long getTempFileLength() {
            return tempFile.length();
        }

        private void deleteTempFileInfoAndTempFile(String desc, boolean throwException) {
            deleteTempFileInfoIfExist();
            if (tempFile.exists()) {
                boolean deleted = tempFile.delete();
                LOG.info("无法断点续传的临时文件" + tempFile.getName() + "删除" + (deleted ? "成功" : "失败") + "(" + desc + ")");
            }
            if (throwException) {
                throw new RuntimeException(desc);
            }
        }

    }
}
