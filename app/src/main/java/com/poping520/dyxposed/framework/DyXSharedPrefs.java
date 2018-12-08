package com.poping520.dyxposed.framework;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.ArrayMap;

import com.poping520.dyxposed.os.AndroidOS;
import com.poping520.dyxposed.util.Objects;

import java.util.HashMap;
import java.util.Map;


/**
 * @author WangKZ
 * @version 1.0.0
 * create on 2018/12/6 15:12
 */
public class DyXSharedPrefs {

    static class Cache {

        private static final Map<String, DyXSharedPrefs> CACHED_MAP;

        static {
            if (AndroidOS.API_LEVEL >= Build.VERSION_CODES.KITKAT)
                CACHED_MAP = new ArrayMap<>();
            else
                CACHED_MAP = new HashMap<>();
        }

        static DyXSharedPrefs getDyXSharedPrefs(String name) {
            DyXSharedPrefs sp = CACHED_MAP.get(name);
            if (sp == null) {
                sp = new DyXSharedPrefs(
                        DyXContext.getApplicationContext().getSharedPreferences(name, Context.MODE_PRIVATE)
                );
                CACHED_MAP.put(name, sp);
            }
            return sp;
        }

        private static <T> void put(SharedPreferences sp, String key, T value) {
            final SharedPreferences.Editor edit = sp.edit();
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
        private static <T> T get(SharedPreferences sp, String key, T defaultValue) {
            Objects.requireNonNull(defaultValue, "must NonNull");
            if (defaultValue instanceof String)
                return (T) sp.getString(key, (String) defaultValue);
            else if (defaultValue instanceof Integer)
                return (T) Integer.valueOf(sp.getInt(key, (Integer) defaultValue));
            else if (defaultValue instanceof Boolean)
                return (T) Boolean.valueOf(sp.getBoolean(key, (Boolean) defaultValue));
            else
                throw new IllegalArgumentException("not implement");
        }
    }

    private SharedPreferences mSP;

    private DyXSharedPrefs(SharedPreferences sp) {
        mSP = sp;
    }

    /**
     * 保存简单键值对数据到指定的 SharedPreferences
     *
     * @param key   key
     * @param value value
     */
    public <T> void put(String key, T value) {
        Cache.put(mSP, key, value);
    }


    /**
     * 从指定的 SharedPreferences 中取数据
     *
     * @param key          key
     * @param defaultValue default value
     * @return value
     */
    public <T> T get(String key, T defaultValue) {
        return Cache.get(mSP, key, defaultValue);
    }
}