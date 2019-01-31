package com.poping520.dyxposed.framework;

import android.os.Environment;
import android.text.TextUtils;

import com.poping520.dyxposed.exception.DyXRuntimeException;
import com.poping520.dyxposed.log.DyXLog;
import com.poping520.dyxposed.model.Module;
import com.poping520.dyxposed.os.AndroidOS;
import com.poping520.dyxposed.os.Shell;
import com.poping520.dyxposed.util.CryptoUtil;
import com.poping520.dyxposed.util.FileUtil;
import com.poping520.dyxposed.util.Objects;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;

import dalvik.system.PathClassLoader;

/**
 * WORK DIR     /data/data/package_name/files
 * API DIR      WORK DIR/api
 * CLASS DIR    WORK DIR/class
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

    private static final String ROOT_RELATIVE_DIR = "shared";

    private static final String MODULE_RELATIVE_DIR = "module";

    // 工作&普通模式 目录
    @Deprecated
    private static final String WORK_SD_DIR =
            Environment.getExternalStorageDirectory().getAbsolutePath() + DYXPOSED_RELATIVE_DIR;

    // 编译环境目录
    static final String COMPILE_ENV = DyXContext.getFilesDir().getAbsolutePath();

    private static final String LIB_DYXPOSED_RELATIVE_PATH = "lib/lib-dyxposed.jar";

    private static final String SPK_WORK_MODE = "WorkMode";
    private static final String SPK_DEVICE_ROOT = "DeviceRoot";

    private static final String SPK_USING_ROOT = "UsingRoot";

    private static class InnerHolder {
        private static final Env INSTANCE = new Env();
    }

    public static Env getInstance() {
        return InnerHolder.INSTANCE;
    }

    private Env() {
    }

    private EnvStateListener mListener;

    void setEnvStateListener(EnvStateListener listener) {
        mListener = listener;

        if (isWorkModeNotConfigure())
            return;

        final boolean now = AndroidOS.isRootedDevice();
        final boolean last = DyXContext.get(ROOT_RELATIVE_DIR, false);

        if (now != last)
            listener.onRootStateChanged(now);
    }

    interface EnvStateListener {

        /**
         * 工作模式已配置
         */
        void onWorkModeConfigured(int mode);

        /**
         * 设备 Root 状态改变
         */
        void onRootStateChanged(boolean isRooted);
    }

    public void setUsingRoot(boolean usingRoot) {
        final Boolean last = DyXContext.get(SPK_USING_ROOT, false);
        DyXContext.put(SPK_USING_ROOT, usingRoot);

    }

    /**
     * 设置工作模式
     *
     * @param mode {@link #MODE_ROOT} or {@link #MODE_NORMAL}
     */
    public void setWorkMode(int mode) {
        DyXContext.put(SPK_WORK_MODE, mode);
        mListener.onWorkModeConfigured(mode);
    }

    /**
     * @return 工作模式 {@link #MODE_ROOT} or {@link #MODE_NORMAL}
     */
    public int getWorkMode() {
        return DyXContext.get(SPK_WORK_MODE, MODE_NORMAL);
    }

    /**
     * @return 是否为 ROOT 工作模式
     */
    public boolean isRootWorkMode() {
        return getWorkMode() == MODE_ROOT;
    }

    /**
     * @return 是否已配置工作模式
     */
    public boolean isWorkModeNotConfigure() {
        return DyXContext.get(SPK_WORK_MODE, MODE_NOT_CONFIGURE) == MODE_NOT_CONFIGURE
                || DyXContext.isLaunchFirstTime();
    }

    /**
     * @return 获取 DyXposed 模块的类加载器
     */
    public ClassLoader getDyXModuleClassLoader() {
        return new PathClassLoader(LibraryAssets.LIB_XPOSED.release(), getClass().getClassLoader());
    }

    /**
     * @return class(tmp) 输出目录
     */
    static String getClassOutputDir() {
        final File dir = new File(DyXContext.getCacheDir(), "class");
        FileUtil.mkDirIfNotExists(dir, true);
        return dir.getAbsolutePath();
    }

    /**
     * @return dex(tmp) 输出路径
     */
    static String getDexOutputPath() {
        String name = String.valueOf(System.currentTimeMillis());
        final String md5 = CryptoUtil.getStringMD5(name);
        if (!TextUtils.isEmpty(md5)) {
            name = md5;
        }
        return new File(DyXContext.getCacheDir(), name + ".jar")
                .getAbsolutePath();
    }

    /**
     * NORMAL => /{$ExternalStorageDir}/Dyxposed
     * ROOT   => /{$AppDataDir}/shared
     */
    private File getWorkDir() {
        if (isRootWorkMode()) {
            final File appDataDir = DyXContext.getAppDataDir();
            final File file = new File(appDataDir, ROOT_RELATIVE_DIR);
            if (!file.exists()) {
                Shell.exec(true, true,
                        "chmod 701 " + appDataDir.getAbsolutePath());
                FileUtil.mkDirIfNotExists(file, true);
                makeGlobal(file.getAbsolutePath());
            }
            return file;
        } else {
            return new File(WORK_SD_DIR);
        }
    }

    /**
     * @return {@link #getWorkDir()} /module
     */
    private File getModuleDir() {
        File file;
        file = new File(getWorkDir(), MODULE_RELATIVE_DIR);
        if (isRootWorkMode()) {
            if (!file.exists()) {
                FileUtil.mkDirIfNotExists(file, true);
                makeGlobal(file.getAbsolutePath());
            }
        } else {
            FileUtil.mkDirIfNotExists(file, true);
        }
        return file;
    }

    /**
     * UI接口 开启模块
     *
     * @param module
     * @param dexBytes
     */
    public void openModule(Module module, byte[] dexBytes) throws DyXRuntimeException, JSONException, IOException {
        if (Objects.isEmptyArray(dexBytes))
            throw new DyXRuntimeException();

        DyXLog.i("open module => " + ModuleHelper.getFullName(module));

        String name = ModuleHelper.getReleaseFileName(module);

        final String jsonStr = ModuleHelper.toJSONString(module);
        final String format = getModuleDir() + File.separator + name + ".%s";
        String jsonPath = String.format(format, "json");
        String jarPath = String.format(format, "jar");

        FileUtil.writeStringToFile(jsonPath, jsonStr, true);
        FileUtil.writeBytes(jarPath, dexBytes, true);

        // 设置权限
        if (isRootWorkMode()) {
            makeGlobal(jsonPath, jarPath);
        }

        releaseDyXLibIfNotExists();
    }

    /**
     * 将 {@link LibraryAssets#API_DYXPOSED} 转为 dex
     * 载入模块时需要的运行依赖
     *
     * @throws DyXRuntimeException
     */
    private void releaseDyXLibIfNotExists() throws DyXRuntimeException {
        final File file = new File(Env.getInstance().getWorkDir(), LIB_DYXPOSED_RELATIVE_PATH);
        final File parentDir = file.getParentFile();
        if (!parentDir.exists()) {
            FileUtil.mkDirIfNotExists(parentDir, true);
        }

        final String absPath = file.getAbsolutePath();
        if (file.exists()) {
            return;
        }

        if (DyXCompiler.dx(LibraryAssets.API_DYXPOSED.release(), absPath)) {
            if (Env.getInstance().isRootWorkMode()) {
                makeGlobal(parentDir.getAbsolutePath(), absPath);
            }
        } else {
            throw new DyXRuntimeException("");
        }
    }

    /**
     * UI接口 关闭模块
     *
     * @param module
     * @throws DyXRuntimeException
     */
    public void closeModule(Module module) throws DyXRuntimeException {
        DyXLog.i("close module => " + ModuleHelper.getFullName(module));

        String name = ModuleHelper.getReleaseFileName(module);
        final File[] files = getModuleDir().listFiles(
                pathname -> pathname.getName().startsWith(name));

        if (Objects.isNonEmptyArray(files)) {
            for (File file : files) {
                FileUtil.remove(file);
            }
        }
    }

    /**
     * 将文件/夹 改为任何 uid 可读可执行
     *
     * @param paths
     * @return
     */
    private static Shell.Result makeGlobal(String... paths) {

        StringBuilder chmod = new StringBuilder();
        StringBuilder chown = new StringBuilder();
        StringBuilder chcon = new StringBuilder();

        chmod.append("chmod 777");
        chown.append("chown 9997:9997");
        chcon.append("chcon u:object_r:media_rw_data_file:s0");
        for (String path : paths) {
            chmod.append(" ").append(path);
            chown.append(" ").append(path);
            chcon.append(" ").append(path);
        }

        return Shell.exec(true, true,
                chmod.toString(),
                chown.toString(),
                AndroidOS.isNewApi_N()
                        ? chcon.toString()
                        : "\n"
        );
    }
}
