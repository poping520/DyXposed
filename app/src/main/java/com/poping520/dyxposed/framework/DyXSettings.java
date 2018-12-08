package com.poping520.dyxposed.framework;

import android.support.v7.preference.PreferenceDataStore;

import com.poping520.dyxposed.R;

/**
 * 程序设置
 *
 * @author WangKZ
 * @version 1.0.0
 * create on 2018/12/6 15:08
 */
public final class DyXSettings extends PreferenceDataStore {

    public static final DyXSettings INSTANCE = new DyXSettings();

    private static final String SHARED_PREFERENCES_NAME = "dyxposed_settings";

    private static final DyXSharedPrefs SP = DyXContext.getDyXSharedPrefs(SHARED_PREFERENCES_NAME);

    @Override
    public void putBoolean(String key, boolean value) {
        SP.put(key, value);
    }

    public static boolean isUsingRoot() {
        return SP.get(DyXContext.getStringFromRes(R.string.pref_key_use_root), false);
    }

    public static boolean isKillTarget() {
        return SP.get(DyXContext.getStringFromRes(R.string.pref_key_kill_target), false);
    }
}
