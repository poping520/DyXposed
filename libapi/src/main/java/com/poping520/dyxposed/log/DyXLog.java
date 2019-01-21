package com.poping520.dyxposed.log;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 日志记录类
 *
 * @author WangKZ
 * @version 1.0.0
 * create on 2019/1/11 14:22
 */
public class DyXLog {

    private static final String DEFAULT_TAG = "DyXposed";

    public static void i(String msg) {
        i(DEFAULT_TAG, msg);
    }

    public static void w(String msg) {
        w(DEFAULT_TAG, msg);
    }

    public static void e(String msg) {
        e(DEFAULT_TAG, msg);
    }

    public static void i(String tag, String msg) {
        log(DyXLogLevel.INFO, tag, msg);
    }

    public static void w(String tag, String msg) {
        log(DyXLogLevel.WARN, tag, msg);
    }

    public static void e(String tag, String msg) {
        log(DyXLogLevel.ERROR, tag, msg);
    }

    private static synchronized void log(DyXLogLevel level, String tag, String msg) {

        final String log = String.format("%s %s/%s: %s\n",
                getCurrentTime(), level.getLevel(), tag, msg);

        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter("/data/data/com.poping520.dyxposed/shared/dyxposed.log", true));
            bw.write(log);
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static String getCurrentTime() {
        return new SimpleDateFormat("yyyy-dd-MM hh:mm:ss.SSS", Locale.getDefault()).format(new Date());
    }
}
