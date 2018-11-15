package com.poping520.dyxposed.util;

import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author WangKZ
 * @version 1.0.0
 * create on 2018/11/14 17:30
 */
public final class JSON {

    public static String stringMap2json(Map<String, String> map) {
        Objects.requireNonNull(map);

        final JSONObject jObj = new JSONObject();

        final Set<Map.Entry<String, String>> entries = map.entrySet();

        for (Map.Entry<String, String> entry : entries) {
            try {
                jObj.put(entry.getKey(), entry.getValue());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return jObj.toString();
    }

    @Nullable
    public static Map<String, String> json2StringMap(String json) {
        Map<String, String> map = null;

        try {
            final JSONObject jObj = new JSONObject(json);
            final Iterator<String> keys = jObj.keys();
            if (keys != null) {
                map = new HashMap<>();
                while (keys.hasNext()) {
                    final String key = keys.next();
                    map.put(key, jObj.optString(key));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return map;
    }

    public static String stringArray2json(String[] strArr) {
        Objects.requireNonNull(strArr);

        final JSONArray jArr = new JSONArray();

        for (String str : strArr) {
            jArr.put(str);
        }
        return jArr.toString();
    }

    @Nullable
    public static String[] json2StringArray(String json) {
        String[] strArr = null;
        try {
            final JSONArray jArr = new JSONArray(json);
            int length = jArr.length();

            if (length > 0) {
                strArr = new String[length];
                for (int i = 0; i < jArr.length(); i++) {
                    strArr[i] = jArr.optString(i);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return strArr;
    }
}
