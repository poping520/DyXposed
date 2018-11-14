package com.poping520.dyxposed.framework;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.StringRes;

import com.poping520.dyxposed.BuildConfig;
import com.poping520.dyxposed.exception.DyXRuntimeException;

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

    public static String getString(@StringRes int resId) {
        return getApplicationContext().getString(resId);
    }

    public static String getString(@StringRes int resId, Object... formatArgs) {
        return getApplicationContext().getString(resId, formatArgs);
    }

    public static SharedPreferences getAppSharedPrefs() {
        return getApplicationContext().getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
    }

    public static boolean isLaunchFirstTime() {
        return getAppSharedPrefs().getInt(SPK_LAUNCH_TIMES, 0) == 0;
    }
}
