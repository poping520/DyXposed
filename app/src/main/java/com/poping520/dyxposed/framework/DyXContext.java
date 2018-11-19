package com.poping520.dyxposed.framework;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;

import com.poping520.dyxposed.BuildConfig;
import com.poping520.dyxposed.exception.DyXRuntimeException;
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

    public static final String APPLICATION_NAME = "DyXposed";

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
        final SharedPreferences sp = getAppSharedPrefs();
        int lastTimes = sp.getInt(SPK_LAUNCH_TIMES, 0);
        sp.edit().putInt(SPK_LAUNCH_TIMES, ++lastTimes).apply();
    }

    public static Context getApplicationContext() {
        final Context context = getInstance().mHolder.mApplicationContext;
        if (context == null)
            //TODO
            throw new DyXRuntimeException("...");

        return context;
    }

    public static File getCacheDir() {
        return getApplicationContext().getCacheDir();
    }

    public static String getStringFromRes(@StringRes int resId) {
        return getApplicationContext().getString(resId);
    }

    public static String getStringFromRes(@StringRes int resId, Object... formatArgs) {
        return getApplicationContext().getString(resId, formatArgs);
    }

    private static SharedPreferences getAppSharedPrefs() {
        return getApplicationContext().getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
    }

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

    static boolean isLaunchFirstTime() {
        return getAppSharedPrefs().getInt(SPK_LAUNCH_TIMES, 0) == 0;
    }
}
