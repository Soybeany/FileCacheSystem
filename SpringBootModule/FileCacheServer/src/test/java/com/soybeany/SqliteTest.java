package com.soybeany;

import com.soybeany.system.cache.core.security.model.FcException;
import com.soybeany.util.file.BdFileUtils;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SqliteTest {

    @Test
    public void test() throws Exception {
        Class.forName("org.sqlite.JDBC");
        try (Connection connection = getConnection()) {
            ResultSet set = connection.prepareStatement("select * from main.sqlite_master").executeQuery();
            while (set.next()) {
                String value = set.getString("sql");
                System.out.println(value);
            }
        }
    }

    @Test
    public void testFile() throws Exception {
        File file = new File("C:\\Users\\soybeany\\Desktop\\test.txt");
        for (int i = 0; i < 50; i++) {
            Thread t1 = new Thread(() -> readFile(file));
            Thread t2 = new Thread(() -> writeFile(file));
            t2.start();
            t1.start();
        }
        Thread.sleep(1000);
    }

    private synchronized void readFile(File file) {
        try (InputStream is = Files.newInputStream(file.toPath())) {
            String s = BdFileUtils.readString(is);
            System.out.println(s);
        } catch (IOException e) {
            throw new FcException(e);
        }
    }

    private synchronized void writeFile(File file) {
        String input = System.currentTimeMillis() + "";
        try (InputStream is = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
             OutputStream os = Files.newOutputStream(file.toPath())) {
            BdFileUtils.readWriteStream(is, os);
        } catch (IOException e) {
            throw new FcException(e);
        }
    }

    Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + "d:/cache-test/cache_server.db");
    }

}
