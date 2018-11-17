package com.poping520.dyxposed.util;

import java.util.List;

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
}
