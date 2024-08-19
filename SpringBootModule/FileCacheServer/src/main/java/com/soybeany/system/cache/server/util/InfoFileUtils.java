package com.soybeany.system.cache.server.util;

import com.google.gson.Gson;
import com.soybeany.util.file.BdFileUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Optional;

public class InfoFileUtils {

    private static final Gson GSON = new Gson();

    public static void write(File infoFile, Object info) {
        try (ByteArrayInputStream stream = new ByteArrayInputStream(GSON.toJson(info).getBytes(StandardCharsets.UTF_8))) {
            BdFileUtils.readWriteStream(stream, infoFile);
        } catch (IOException e) {
            throw new RuntimeException("写入info文件异常:" + e.getMessage());
        }
    }

    public static <T> Optional<T> read(File infoFile, Class<T> clazz) {
        try (InputStream is = Files.newInputStream(infoFile.toPath());
             ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            BdFileUtils.readWriteStream(is, stream);
            return Optional.of(GSON.fromJson(stream.toString("utf-8"), clazz));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

}
