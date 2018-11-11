package com.poping520.dyxposed.framework;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import com.poping520.dyxposed.exception.DyXRuntimeException;
import com.poping520.dyxposed.util.FileUtil;

import java.io.File;
import java.io.IOException;

/**
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


    private static final String ENV_PATH =
            Environment.getExternalStorageDirectory().getAbsolutePath() + "/DyXposed";


    private static final String SPK_WORK_MODE = "WorkMode";

    public enum Api {

        XPOSED_API("api/xposed-api-82.jar"),

        ANDROID_RT("api/android-28.jar");

        private String assetPath;

        Api(String assetPath) {
            this.assetPath = assetPath;
        }

        String getWorkPath() {
            return ENV_PATH + File.separator + assetPath;
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

    public void init() {

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

    public void setWorkMode(int mode) {
        mAppSp.edit().putInt(SPK_WORK_MODE, mode).apply();
    }

    public int getWorkMode() {
        return mAppSp.getInt(SPK_WORK_MODE, MODE_NORMAL);
    }

    public boolean isWorkModeNotConfigure() {
        return mAppSp.getInt(SPK_WORK_MODE, MODE_NOT_CONFIGURE) == MODE_NOT_CONFIGURE;
    }
}
