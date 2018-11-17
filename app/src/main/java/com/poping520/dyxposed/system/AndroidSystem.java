package com.poping520.dyxposed.system;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import com.poping520.dyxposed.exception.DyXRuntimeException;
import com.poping520.dyxposed.framework.DyXContext;

import java.io.File;
import java.util.Locale;

/**
 * @author WangKZ
 * @version 1.0.0
 * create on 2018/11/9 10:08
 */
public class AndroidSystem {

    /**
     * 当前系统 SDK_INT
     */
    public static final int API_LEVEL = Build.VERSION.SDK_INT;

    public static final String XPOSED_INSTALLER_PACKAGE_NAME = "de.robv.android.xposed.installer";

    /**
     * @return 当前设备的系统是否 ROOT
     */
    public static boolean isRootedDevice() {
        return isSUBinaryExists() || isMagiskInstalled();
    }

    // 是否安装 Magisk 框架
    private static boolean isMagiskInstalled() {
        final Shell.Result ret = Shell.exec(false, false, "magisk -V");
        return new File("/sbin/magisk").exists() || ret.success;
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

    /**
     * 检测是否安装 Xposed 框架
     */
    public static boolean isXposedFrameworkInstalled() {
        try {
            throw new Exception();
        } catch (Exception e) {
            final StackTraceElement[] stackTrace = e.getStackTrace();
            if (stackTrace == null || stackTrace.length == 0) {
                return false;
            }

            for (int i = stackTrace.length - 1; i >= 0; i--) {
                if ("de.robv.android.xposed.XposedBridge".equals(stackTrace[i].getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 检测是否安装 Xposed 管理器
     */
    public static boolean isXposedManagerInstalled() {
        final PackageManager pm = DyXContext.getApplicationContext().getPackageManager();
        try {
            pm.getPackageInfo(XPOSED_INSTALLER_PACKAGE_NAME, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void jump2XposedManager() {
        try {
            final Context context = DyXContext.getApplicationContext();
            ComponentName cn = new ComponentName(XPOSED_INSTALLER_PACKAGE_NAME,
                    XPOSED_INSTALLER_PACKAGE_NAME + ".WelcomeActivity");
            final Intent intent = new Intent();
            intent.setComponent(cn);
            context.startActivity(intent);

        } catch (Exception e) {
            e.printStackTrace();
            // TODO
            throw new DyXRuntimeException();
        }
    }

    /**
     * @return 当前系统语言
     */
    public static String getCurrentLanguage() {
        return Locale.getDefault().getLanguage();
    }

    public static boolean isNewApi_N() {
        return API_LEVEL >= Build.VERSION_CODES.N;
    }
}
