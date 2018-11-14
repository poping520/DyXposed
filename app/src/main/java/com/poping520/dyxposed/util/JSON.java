package com.poping520.dyxposed.util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.Set;

/**
 * @author WangKZ
 * @version 1.0.0
 * create on 2018/11/14 17:30
 */
public final class JSON {

    public static String map2json(Map<String, String> map) {
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
}
