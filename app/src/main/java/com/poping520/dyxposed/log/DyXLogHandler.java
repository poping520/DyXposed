package com.poping520.dyxposed.log;

import com.poping520.dyxposed.framework.DyXContext;

import java.io.File;

/**
 * @author WangKZ
 * @version 1.0.0
 * create on 2019/1/21 14:35
 */
public class DyXLogHandler {

    private static final String LOG_FILE = "DyXposed.log";

    public static DyXLogLevel getLogLevel(String line) {
        // 2019-21-01 01:48:06.208 I/DyXposed: open module => 测试模块(dyxposed-test)
        // 由第 24 个字符得出日志等级
        final char ch = line.charAt(24);
        return DyXLogLevel.parseLevel(String.valueOf(ch));
    }

    public static String getLogTag(String line) {
        return line.substring(26, line.length()).split(":")[0];
    }

    public static File getLogFile() {
        return new File(DyXContext.getCacheDir(), LOG_FILE);
    }
}
