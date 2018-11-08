package com.poping520.dyxposed.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by WangKZ on 18/11/07.
 *
 * @author poping520
 * @version 1.0.0
 */
public class DateUtil {

    public static String getCurrentTime(String format) {
        return new SimpleDateFormat(format, Locale.getDefault()).format(new Date());
    }
}
