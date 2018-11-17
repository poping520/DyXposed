package com.poping520.dyxposed.util;

import com.poping520.dyxposed.model.Module;
import com.poping520.dyxposed.system.AndroidSystem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.Set;

/**
 * Created by WangKZ on 18/11/15.
 *
 * @author poping520
 * @version 1.0.0
 */
public class ModuleUtil {

    /**
     * 从 {@link Module#name} 中获取模块名称
     * <p>
     * 若 {@link Module#name} 为空映射或者匹配不到当前系统的语言,
     * 则返回 {@link Module#id}
     */
    public static String getShowName(Module module) {
        final Map<String, String> map = module.name;

        if (map == null || map.isEmpty())
            return module.id;
        else {
            final Set<Map.Entry<String, String>> entries = map.entrySet();
            String name = null;

            for (Map.Entry<String, String> entry : entries) {
                if (AndroidSystem.getCurrentLanguage().equals(entry.getKey())) {
                    name = entry.getValue();
                    break;
                }
            }
            return name == null ? module.id : name;
        }
    }

    /**
     * 从 {@link Module#desc} 中获取模块描述
     * <p>
     * 若 {@link Module#desc} 为空映射或者匹配不到当前系统的语言,
     * 则返回空字符串
     */
    public static String getShowDesc(Module module) {
        final Map<String, String> desc = module.desc;
        String str = "";
        if (desc != null) {
            final Set<Map.Entry<String, String>> entries = desc.entrySet();

            for (Map.Entry<String, String> entry : entries) {
                if (AndroidSystem.getCurrentLanguage().equals(entry.getKey())) {
                    str = entry.getValue();
                    break;
                }
            }
        }
        return str;
    }


    public static String toJSONString(Module module) throws JSONException {
        final JSONObject jObj = new JSONObject();
        final JSONArray jArr = new JSONArray();

        final String[] target = module.target;
        if (target != null) {
            for (String str : target) {
                jArr.put(str);
            }
        }

        jObj.put("target", jArr);
        jObj.put("entryClass", module.entryClass);
        jObj.put("entryMethod", module.entryMethod);

        return jObj.toString();
    }
}
