package com.poping520.dyxposed.system;

import android.os.Build;

import java.io.File;

/**
 * @author WangKZ
 * @version 1.0.0
 * create on 2018/11/9 10:08
 */
public class AndroidSystem {

    public static final int API_LEVEL = Build.VERSION.SDK_INT;


    public static boolean isRootedDevice() {
        return isSUBinaryExists() || isMagiskInstalled();
    }

    private static boolean isMagiskInstalled() {
        final Shell.Result ret = Shell.exec(false, false, "magisk -V");
        return new File("/sbin/magisk").exists() || ret.isSuccess;
    }

    // 检查 su 二进制文件是否存在
    private static boolean isSUBinaryExists() {
        final String[] places = {
                "/sbin/su",
                "/system/bin/su",
                "/system/xbin/su",
                "/data/local/xbin/su",
                "/data/local/bin/su",
                "/system/sd/xbin/su",
                "/system/bin/failsafe/su",
                "/data/local/su",
                "/data/su.img", //system-less模式
        };
        for (String place : places) {
            if (new File(place).exists()) {
                return true;
            }
        }
        return false;
    }
}
