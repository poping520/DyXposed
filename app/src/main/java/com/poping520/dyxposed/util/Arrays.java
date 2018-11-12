package com.poping520.dyxposed.util;

/**
 * Created by WangKZ on 18/11/12.
 *
 * @author poping520
 * @version 1.0.0
 */
public class Arrays {

    public static <T> boolean isNullArray(T[] arr) {
        return arr == null || arr.length == 0;
    }

    public static <T> boolean isNonNullArray(T[] arr) {
        return arr != null && arr.length > 0;
    }
}
