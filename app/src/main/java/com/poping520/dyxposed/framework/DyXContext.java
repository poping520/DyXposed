package com.poping520.dyxposed.framework;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

import com.poping520.dyxposed.BuildConfig;
import com.poping520.dyxposed.R;
import com.poping520.dyxposed.util.Objects;
import com.poping520.open.mdialog.MDialog;

import java.io.File;
import java.util.Map;

/**
 * @author WangKZ
 * @version 1.0.0
 * create on 2018/11/9 10:16
 */
public final class DyXContext {

    private static final class InnerHolder {

        private static final DyXContext INSTANCE = new DyXContext();

        /* 全局 Context (Application Context) 对象 */
        private Context applicationContext;

        /* 当前 Activity 对象*/
        private Activity currentActivity;
    }

    /**
     * 本程序的名称
     */
    public static final String APPLICATION_NAME = "DyXposed";

    private static final String SPK_LAUNCH_TIMES = "LaunchTimes";

    private static DyXSharedPrefs sAppSP;

    private final InnerHolder mHolder;

    /* Activity Object MAP */
    private final Map<Integer, Activity> mActMap;

    private DyXContext() {
        mHolder = new InnerHolder();
        mActMap = Objects.obtainEmptyMap();
    }

    static DyXContext getInstance() {
        return InnerHolder.INSTANCE;
    }

    void onCreate(Application app) {
        mHolder.applicationContext = app.getApplicationContext();
        sAppSP = getAppDyXSharedPrefs();

        // 更新启动次数
        int lastTimes = sAppSP.get(SPK_LAUNCH_TIMES, 0);
        sAppSP.put(SPK_LAUNCH_TIMES, ++lastTimes);
    }

    void onCreate(Activity act) {
        Objects.requireNonNull(act, "activity object is NULL");
        mHolder.currentActivity = act;
        mActMap.put(act.hashCode(), act);
    }

    void onDestroy(Activity act) {
        Objects.requireNonNull(act, "activity object is NULL");
        mActMap.remove(act.hashCode());
    }

    private static void onAppDestroy() {
        // 关闭数据库
        DyXDBHelper.getInstance().release();
    }

    /**
     * @return 全局 Context (Application Context) 对象
     */
    public static Context getApplicationContext() {
        final Context context = getInstance().mHolder.applicationContext;
        if (context == null)
            throw new IllegalStateException("...");

        return context;
    }

    /**
     * @return 程序当前界面的 Activity 对象
     */
    public static Activity getCurrentActivity() {
        final Activity activity = getInstance().mHolder.currentActivity;
        if (activity == null)
            throw new IllegalStateException("Launch Activity is not initialize");

        return activity;
    }

    /**
     * 任何时候调用 即可正常退出程序
     */
    public static void safeExitApp() {
        onAppDestroy();

        final Map<Integer, Activity> actMap = getInstance().mActMap;
        for (Map.Entry<Integer, Activity> entry : actMap.entrySet()) {
            entry.getValue().finish();
        }
    }

    public static MDialog.Builder buildMDialog(@DrawableRes int headerPic, @StringRes int title) {
        return new MDialog.Builder(getCurrentActivity())
                .setHeaderBgColorRes(R.color.colorPrimary)
                .setHeaderPic(headerPic)
                .setTitle(title);
    }

    /**
     * @return 本程序私有数据目录
     */
    public static File getAppDataDir() {
        return getCacheDir().getParentFile();
    }

    /**
     * @return {@link Context#getFilesDir()}
     */
    public static File getFilesDir() {
        return getApplicationContext().getFilesDir();
    }

    /**
     * @return {@link Context#getCacheDir()}
     */
    public static File getCacheDir() {
        return getApplicationContext().getCacheDir();
    }

    /**
     * @return {@link Context#getString(int)}
     */
    public static String getStringFromRes(@StringRes int resId) {
        return getApplicationContext().getString(resId);
    }

    /**
     * @return {@link Context#getString(int, Object...)}
     */
    public static String getStringFromRes(@StringRes int resId, Object... formatArgs) {
        return getApplicationContext().getString(resId, formatArgs);
    }

    /**
     * @param name 文件名
     * @return {@link DyXSharedPrefs}
     */
    public static DyXSharedPrefs getDyXSharedPrefs(String name) {
        return DyXSharedPrefs.Cache.getDyXSharedPrefs(name);
    }


    public static DyXSharedPrefs getAppDyXSharedPrefs() {
        return getDyXSharedPrefs(BuildConfig.APPLICATION_ID);
    }

    public static void setRootAuthState(boolean granted) {

    }

    public static boolean isRootAuthGranted() {
        return false;
    }

    /**
     * 保存简单键值对数据到程序默认的 SharedPreferences
     *
     * @param key   key
     * @param value value
     */
    @Deprecated
    public static <T> void put(String key, T value) {
        sAppSP.put(key, value);
    }

    /**
     * 从程序默认的 SharedPreferences 中取数据
     *
     * @param key          key
     * @param defaultValue default value
     * @return value
     */
    @Deprecated
    public static <T> T get(String key, T defaultValue) {
        return sAppSP.get(key, defaultValue);
    }

    /**
     * @return 程序是否首次启动
     */
    static boolean isLaunchFirstTime() {
        return sAppSP.get(SPK_LAUNCH_TIMES, 0) == 0;
    }
}
