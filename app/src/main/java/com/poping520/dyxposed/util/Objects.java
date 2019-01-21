package com.poping520.dyxposed.util;

import android.os.Build;
import android.util.ArrayMap;

import com.poping520.dyxposed.os.AndroidOS;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by WangKZ on 18/11/12.
 *
 * @author poping520
 * @version 1.0.0
 */
public class Objects {

    public static <T> T requireNonNull(T obj, String message) {
        if (obj == null)
            throw new NullPointerException(message);
        return obj;
    }

    public static boolean isEmptyArray(byte[] arr) {
        return arr == null || arr.length == 0;
    }

    public static <T> boolean isEmptyArray(T[] arr) {
        return arr == null || arr.length == 0;
    }

    public static <T> boolean isNonEmptyArray(T[] arr) {
        return arr != null && arr.length > 0;
    }

    public static <T> boolean isEmptyList(List<T> list) {
        return list == null || list.isEmpty();
    }

    public static <T> boolean isNonEmptyList(List<T> list) {
        return list != null && !list.isEmpty();
    }

    public static <K, V> Map<K, V> obtainEmptyMap() {
        if (AndroidOS.API_LEVEL >= Build.VERSION_CODES.KITKAT)
            return new ArrayMap<>();
        else
            return new HashMap<>();
    }
}
