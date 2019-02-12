package com.poping520.dyxposed.os;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;

import com.poping520.dyxposed.R;
import com.poping520.dyxposed.exception.DyXRuntimeException;
import com.poping520.dyxposed.framework.DyXContext;
import com.poping520.dyxposed.util.FileUtil;

import java.io.File;
import java.util.List;
import java.util.Locale;

/**
 * Android OS
 * <p>
 * 封装 Android 操作系统的相关方法
 * 本类没有实例对象, 全部采用静态调用
 *
 * @author WangKZ
 * @version 1.0.0
 * create on 2018/11/9 10:08
 */
public final class AndroidOS {

    /**
     * 当前系统 SDK_INT
     */
    public static final int API_LEVEL = Build.VERSION.SDK_INT;

    public static final String XPOSED_INSTALLER_PACKAGE_NAME = "de.robv.android.xposed.installer";

    // 不可创建类对象
    private AndroidOS() {

    }

    /**
     * @return 当前设备的系统是否 ROOT
     */
    public static boolean isDeviceRooted() {
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
     * 检测是否安装 Xposed Installer
     */
    public static boolean isXposedInstallerInstalled() {
        final PackageManager pm = DyXContext.getApplicationContext().getPackageManager();
        try {
            pm.getPackageInfo(XPOSED_INSTALLER_PACKAGE_NAME, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 跳转到 Xposed Installer
     */
    public static void jump2XposedInstaller() {
        final Context context = DyXContext.getApplicationContext();

        if (!isXposedInstallerInstalled()) {
            Toast.makeText(context, R.string.xposed_installer_not_installed, Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            ComponentName cn = new ComponentName(XPOSED_INSTALLER_PACKAGE_NAME,
                    XPOSED_INSTALLER_PACKAGE_NAME + ".WelcomeActivity");
            final Intent intent = new Intent();
            intent.setComponent(cn);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, R.string.jump_xposed_installer_error, Toast.LENGTH_SHORT).show();
        }
    }

    // 懒加载 单例模式
    private static ProcessManager sProcessManager;

    public static ProcessManager getProcessManager() {
        if (sProcessManager == null) {
            sProcessManager = new ProcessManager();
        }
        return sProcessManager;
    }

    public static void forceStopApp(String packageName) {
        String cmd = "am force-stop " + packageName;
        Shell.exec(true, false, cmd);
    }

    /**
     * @return 获取当前调用本方法的进程名
     */
    public static String getCurrentProcessName() {
        final File file = new File("/proc/self/cmdline");
        String processName = FileUtil.readTextFile(file);
        if (processName == null) processName = "";
        return processName;
    }

    public static void killProcess(String packageName) {
    }

    /**
     * 杀死本程序进程
     */
    public static void killSelf() {
        DyXContext.safeExitApp();
        Process.killProcess(Process.myPid());
    }

    /**
     * @return 当前系统语言
     */
    public static String getCurrentLanguage() {
        return Locale.getDefault().getLanguage();
    }

    public static boolean isApiLevelUp_N() {
        return API_LEVEL >= Build.VERSION_CODES.N;
    }
}
