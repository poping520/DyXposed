package com.poping520.dyxposed.framework;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.poping520.dyxposed.exception.DyXRuntimeException;
import com.poping520.dyxposed.system.AndroidSystem;
import com.poping520.dyxposed.system.Shell;
import com.poping520.dyxposed.util.CryptoUtil;
import com.poping520.dyxposed.util.FileUtil;

import java.io.File;
import java.io.IOException;

import dalvik.system.PathClassLoader;

/**
 * WORK DIR     /sdcard/DyXposed
 * API DIR      /sdcard/DyXposed/api
 * CLASS DIR    /sdcard/DyXposed/class
 * <p>
 * ROOT
 * - DEX DIR  /data/DyXposed/module
 * <p>
 * NORMAL
 * - DEX DIR  /sdcard/DyXposed/module
 *
 * @author WangKZ
 * @version 1.0.0
 * create on 2018/11/9 9:55
 */
public class Env {

    /**
     * ROOT工作模式
     */
    public static final int MODE_ROOT = 0x1;

    /**
     * 普通工作模式
     */
    public static final int MODE_NORMAL = 0x2;

    /**
     * 未确定工作模式
     */
    public static final int MODE_NOT_CONFIGURE = 0x0;


    private static final String DYXPOSED_RELATIVE_DIR = "/DyXposed";

    private static final String DEX_RELATIVE_DIR = "/module";

    // 工作&普通模式 目录
    private static final String WORK_DIR =
            Environment.getExternalStorageDirectory().getAbsolutePath() + DYXPOSED_RELATIVE_DIR;

    // root模式 目录
    private static final String ROOT_DIR =
            Environment.getDataDirectory().getAbsolutePath() + DYXPOSED_RELATIVE_DIR;

    private static final String SPK_WORK_MODE = "WorkMode";

    // class文件输出 目录
    private static final String CLASS_OUTPUT_DIR = WORK_DIR + "/class";

    public enum Api {

        API_XPOSED("api/api-xposed-82.jar", "87B9A136AE65B583E78B02F51C27D4A8"),

        API_ANDROID("api/api-android-28.jar", "F0A233AD65F0B1CC5CA461B404767658"),

        API_DYXPOSED("api/api-dyxposed-1.jar", "03A424395F0F1E25D51FCAD912CFE44D");

        private String assetPath;
        private String md5;

        Api(String assetPath, String md5) {
            this.assetPath = assetPath;
            this.md5 = md5;
        }

        String release() {
            return releaseAsset(assetPath, md5);
        }
    }

    public enum Lib {

        LIB_XPOSED("lib/lib-xposed.jar", "956145163B20889A7D895020F197E813"),

        LIB_DYXPOSED("lib/lib-dyxposed.jar", null);

        private String assetPath;
        private String md5;

        Lib(String assetPath, String md5) {
            this.assetPath = assetPath;
            this.md5 = md5;
        }

        String release() {
            if (LIB_XPOSED.equals(this))
                return releaseAsset(assetPath, md5);
            else
                return releaseDyXposedLib(assetPath);
        }
    }

    private static String releaseDyXposedLib(String relativePath) {
        final File file = new File(WORK_DIR, relativePath);
        final String absPath = file.getAbsolutePath();
        if (file.exists()) {
            return absPath;
        }

        if (DyXCompiler.dx(Api.API_DYXPOSED.release(), absPath)) {
            return absPath;
        } else {
            throw new DyXRuntimeException("");
        }
    }

    private static String releaseAsset(String assetPath, String md5) {
        final File file = new File(WORK_DIR, assetPath);
        if (!FileUtil.verifyMD5(file, md5)) {
            try {
                FileUtil.unZipAsset(DyXContext.getApplicationContext(), assetPath, file.getAbsolutePath(), true);
            } catch (IOException e) {
                e.printStackTrace();
                throw new DyXRuntimeException("");
            }
        }
        return file.getAbsolutePath();
    }


    private SharedPreferences mAppSp;

    private static class InnerHolder {
        private static final Env INSTANCE = new Env();
    }

    public static Env getInstance() {
        return InnerHolder.INSTANCE;
    }

    private Env() {
        mAppSp = DyXContext.getAppSharedPrefs();
    }

    /**
     * 设置工作模式
     *
     * @param mode {@link #MODE_ROOT} or {@link #MODE_NORMAL}
     */
    public void setWorkMode(int mode) {
        mAppSp.edit().putInt(SPK_WORK_MODE, mode).apply();
    }

    /**
     * @return 工作模式 {@link #MODE_ROOT} or {@link #MODE_NORMAL}
     */
    public int getWorkMode() {
        return mAppSp.getInt(SPK_WORK_MODE, MODE_NORMAL);
    }

    /**
     * @return 是否已配置工作模式
     */
    public boolean isWorkModeNotConfigure() {
        return mAppSp.getInt(SPK_WORK_MODE, MODE_NOT_CONFIGURE) == MODE_NOT_CONFIGURE
                || DyXContext.isLaunchFirstTime();
    }

    /**
     * @return 获取 DyXposed 模块的类加载器
     */
    public ClassLoader getDyXModuleClassLoader() {
        return new PathClassLoader(Lib.LIB_XPOSED.release(), getClass().getClassLoader());
    }

    /**
     * @return class 输出目录
     */
    static String getClassOutputDir() {
        FileUtil.mkDirIfNotExists(CLASS_OUTPUT_DIR, true);
        return CLASS_OUTPUT_DIR;
    }

    /**
     * @return dex 输出路径
     */
    public String getDexOutputPath() {
        String name = String.valueOf(System.currentTimeMillis());

        final String md5 = CryptoUtil.getStringMD5(name);
        if (!TextUtils.isEmpty(md5)) {
            name = md5;
        }

        String dexOutputDir;
        // 检查 dex 输出文件夹
        if (getWorkMode() == MODE_NORMAL) {
            dexOutputDir = WORK_DIR + DEX_RELATIVE_DIR;
            FileUtil.mkDirIfNotExists(dexOutputDir, true);

        } else {
            dexOutputDir = ROOT_DIR + DEX_RELATIVE_DIR;
            checkRootModeDexOutputDir();
        }

        return String.format("%s/%s.jar", dexOutputDir, name);
    }

    private boolean checkRootModeDexOutputDir() {
        final File file = new File(ROOT_DIR + DEX_RELATIVE_DIR);
        if (file.exists()) {
            return true;
        }

        String[] cmds = {
                "mkdir -p " + file.getAbsolutePath(),
                "chmod -R 777 " + ROOT_DIR,
                "chown -R 9997:9997 " + ROOT_DIR,
                "\n"
        };

        if (AndroidSystem.API_LEVEL >= Build.VERSION_CODES.N) {
            cmds[3] = "chcon -R u:object_r:media_rw_data_file:s0 " + ROOT_DIR;
        }

        return Shell.exec(true, true, cmds).success;
    }
}
