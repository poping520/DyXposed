package com.poping520.dyxposed.framework;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.StringRes;

import com.poping520.dyxposed.BuildConfig;
import com.poping520.dyxposed.util.Objects;

import java.io.File;

/**
 * @author WangKZ
 * @version 1.0.0
 * create on 2018/11/9 10:16
 */
public class DyXContext {

    private static class InnerHolder {
        private static final DyXContext INSTANCE = new DyXContext();
        private Context mApplicationContext;
        private Activity mMainActivity;
    }

    /**
     * 本程序的名称
     */
    public static final String APPLICATION_NAME = "DyXposed";

    public static final String APP_DB_NAME = DyXContext.APPLICATION_NAME + ".db";

    private static final String SPK_LAUNCH_TIMES = "LaunchTimes";

    private final InnerHolder mHolder;

    private DyXContext() {
        mHolder = new InnerHolder();
    }

    public static DyXContext getInstance() {
        return InnerHolder.INSTANCE;
    }

    void init(Application app) {
        mHolder.mApplicationContext = app.getApplicationContext();
    }

    void init(BaseMainActivity act) {
        mHolder.mMainActivity = act;

        // 更新启动次数
        int lastTimes = get(SPK_LAUNCH_TIMES, 0);
        save(SPK_LAUNCH_TIMES, ++lastTimes);
    }

    /**
     * @return 全局 Context (Application Context) 对象
     */
    public static Context getApplicationContext() {
        final Context context = getInstance().mHolder.mApplicationContext;
        if (context == null)
            throw new IllegalStateException("...");

        return context;
    }

    /**
     * @return 程序私有数据目录
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

    // 程序默认的 SharedPreferences
    private static SharedPreferences getAppSharedPrefs() {
        return getApplicationContext().getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
    }

    /**
     * 保存简单键值对数据到程序默认的 SharedPreferences
     *
     * @param key   key
     * @param value value
     */
    public static <T> void save(String key, T value) {
        final SharedPreferences.Editor edit = getAppSharedPrefs().edit();
        if (value instanceof String)
            edit.putString(key, (String) value);
        else if (value instanceof Integer)
            edit.putInt(key, (Integer) value);
        else if (value instanceof Boolean)
            edit.putBoolean(key, (Boolean) value);
        else
            throw new IllegalArgumentException("not implement");
        edit.apply();
    }

    /**
     * 从程序默认的 SharedPreferences 中取数据
     *
     * @param key          key
     * @param defaultValue default value
     * @return value
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(String key, T defaultValue) {
        Objects.requireNonNull(defaultValue, "must NonNull");
        final SharedPreferences sp = getAppSharedPrefs();
        if (defaultValue instanceof String)
            return (T) sp.getString(key, (String) defaultValue);
        else if (defaultValue instanceof Integer)
            return (T) Integer.valueOf(sp.getInt(key, (Integer) defaultValue));
        else if (defaultValue instanceof Boolean)
            return (T) Boolean.valueOf(sp.getBoolean(key, (Boolean) defaultValue));
        else
            throw new IllegalArgumentException("not implement");
    }

    /**
     * @return 程序是否首次启动
     */
    static boolean isLaunchFirstTime() {
        return get(SPK_LAUNCH_TIMES, 0) == 0;
    }
}
