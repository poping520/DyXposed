package com.poping520.dyxposed.util;

import android.support.annotation.Nullable;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/**
 * @author WangKZ
 * @version 1.0.0
 * create on 2018/11/12 14:45
 */
public class CryptoUtil {

    private static final char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public static String getMD5HexStr(@Nullable String text) {
        if (text == null) return null;

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            return bytes2HexStr(md.digest(text.getBytes())).toLowerCase(Locale.ENGLISH);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String bytes2HexStr(byte[] bytes) {
        if (bytes == null) return null;
        int len = bytes.length;
        if (len <= 0) return null;
        char[] ret = new char[len << 1];
        for (int i = 0, j = 0; i < len; i++) {
            ret[j++] = hexDigits[bytes[i] >>> 4 & 0x0f];
            ret[j++] = hexDigits[bytes[i] & 0x0f];
        }
        return new String(ret);
    }
}
