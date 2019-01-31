package com.poping520.dyxposed.framework;

import android.text.TextUtils;

import com.poping520.dyxposed.model.Module;
import com.poping520.dyxposed.os.AndroidOS;
import com.poping520.dyxposed.util.CryptoUtil;

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
public class ModuleHelper {

    /**
     * 获取模块释放文件的名称
     *
     * @param module 模块对象
     */
    public static String getReleaseFileName(Module module) {
        String fileName = CryptoUtil.getStringMD5(module.id);
        if (TextUtils.isEmpty(fileName)) {
            fileName = module.id;
        }
        return fileName;
    }

    /**
     * 模块名称(模块id)
     *
     * @param module 模块对象
     */
    public static String getFullName(Module module) {
        return String.format("%s(%s)", getShowName(module), module.id);
    }

    /**
     * 从 {@link Module#name} 中获取模块名称
     * <p>
     * 若 {@link Module#name} 为空映射或者匹配不到当前系统的语言,
     * 则返回 name 中的首个元素
     */
    public static String getShowName(Module module) {
        final Map<String, String> map = module.name;

        if (map == null || map.isEmpty())
            return module.id;
        else {
            final Set<Map.Entry<String, String>> entries = map.entrySet();
            String name = null;
            // 备用名称
            String spareName = null;

            for (Map.Entry<String, String> entry : entries) {
                final String value = entry.getValue();
                spareName = value;
                if (AndroidOS.getCurrentLanguage().equals(entry.getKey())) {
                    name = value;
                    break;
                }
            }
            return name == null ? spareName : name;
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
                if (AndroidOS.getCurrentLanguage().equals(entry.getKey())) {
                    str = entry.getValue();
                    break;
                }
            }
        }
        return str;
    }

    /**
     * Module 对象转 JSON 字符串
     *
     * @param module 模块对象
     * @throws JSONException
     */
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
