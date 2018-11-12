package com.poping520.dyxposed.framework;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;

import com.poping520.dyxposed.system.AndroidSystem;
import com.poping520.dyxposed.system.Shell;
import com.poping520.dyxposed.util.CryptoUtil;
import com.poping520.dyxposed.util.FileUtil;

import java.io.File;
import java.io.IOException;

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

        XPOSED_API("api/xposed-api-82.jar"),

        ANDROID_RT("api/android-28.jar");

        private String assetPath;

        Api(String assetPath) {
            this.assetPath = assetPath;
        }

        String getWorkPath() {
            return WORK_DIR + File.separator + assetPath;
        }
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

    void init() {

        // 检查编译依赖库
        for (Api lib : Api.values()) {
            final String workPath = lib.getWorkPath();
            final File libFile = new File(workPath);
            if (!libFile.exists()) {
                try {
                    FileUtil.unZipAsset(DyXContext.getApplicationContext(), lib.assetPath, workPath, true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 设置工作模式
     *
     * @param mode {@link #MODE_ROOT} or {@link #MODE_NORMAL}
     */
    public void setWorkMode(int mode) {
        mAppSp.edit().putInt(SPK_WORK_MODE, mode).apply();
    }

    public int getWorkMode() {
        return mAppSp.getInt(SPK_WORK_MODE, MODE_NORMAL);
    }

    public boolean isWorkModeNotConfigure() {
        return mAppSp.getInt(SPK_WORK_MODE, MODE_NOT_CONFIGURE) == MODE_NOT_CONFIGURE;
    }

    static String getClassOutputDir() {
        FileUtil.mkDirIfNotExists(CLASS_OUTPUT_DIR);
        return CLASS_OUTPUT_DIR;
    }

    /**
     * @return dex 输出路径
     */
    @WorkerThread
    public String getDexOutputPath() {
        String name = String.valueOf(System.currentTimeMillis());

        final String md5 = CryptoUtil.getMD5HexStr(name);
        if (!TextUtils.isEmpty(md5)) {
            name = md5;
        }

        String dexOutputDir;
        // 检查 dex 输出文件夹
        if (getWorkMode() == MODE_NORMAL) {
            dexOutputDir = WORK_DIR + DEX_RELATIVE_DIR;
            FileUtil.mkDirIfNotExists(dexOutputDir);

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

        return Shell.exec(true, true, cmds).isSuccess;
    }
}
