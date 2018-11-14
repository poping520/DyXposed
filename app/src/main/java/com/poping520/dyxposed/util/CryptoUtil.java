package com.poping520.dyxposed.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.Objects;

/**
 * @author WangKZ
 * @version 1.0.0
 * create on 2018/11/12 14:45
 */
public final class CryptoUtil {

    /**
     * @return 字符串 MD5 摘要
     */
    @Nullable
    public static String getStringMD5(String text) {
        Objects.requireNonNull(text);

        try {
            return bytes2HexString(getBytes(text)).toLowerCase(Locale.ENGLISH);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @return 文件 MD5 摘要
     */
    @Nullable
    public static String getFileMD5(File file) {
        Objects.requireNonNull(file);

        if (!file.exists()) return null;

        InputStream is = null;
        String md5 = null;
        try {
            is = new FileInputStream(file);
            md5 = bytes2HexString(getBytes(is));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return md5;
    }

    private static byte[] getBytes(String text) throws NoSuchAlgorithmException {
        return MessageDigest.getInstance("MD5").digest(text.getBytes());
    }

    private static byte[] getBytes(@NonNull InputStream inputStream) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        DigestInputStream dis = new DigestInputStream(inputStream, md);
        byte[] buffer = new byte[256 * 1024];
        while (true) {
            if (!(dis.read(buffer) > 0)) break;
        }
        md = dis.getMessageDigest();
        try {
            dis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return md.digest();
    }

    private static final char HEX_DIGITS[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    /**
     * byte 数组转 16 进制字符串
     */
    public static String bytes2HexString(byte[] bytes) {
        if (bytes == null) return null;
        int len = bytes.length;
        if (len <= 0) return null;
        char[] ret = new char[len << 1];
        for (int i = 0, j = 0; i < len; i++) {
            ret[j++] = HEX_DIGITS[bytes[i] >>> 4 & 0x0f];
            ret[j++] = HEX_DIGITS[bytes[i] & 0x0f];
        }
        return new String(ret);
    }
}
