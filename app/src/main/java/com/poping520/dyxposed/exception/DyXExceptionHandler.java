package com.poping520.dyxposed.exception;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author WangKZ
 * @version 1.0.0
 * create on 2018/11/14 16:52
 */
public class DyXExceptionHandler {

    public static String getStackErrorString(Throwable th) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        th.printStackTrace(pw);
        return sw.toString();
    }
}
