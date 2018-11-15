package com.poping520.dyxposed.util;

import com.poping520.dyxposed.model.Module;

import java.util.Locale;
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
     * 获取模块显示名称
     */
    public static String getShowName(Module module) {
        final Map<String, String> map = module.name;

        if (map == null || map.size() == 0)
            return module.id;
        else {
            final Set<Map.Entry<String, String>> entries = map.entrySet();
            String name = null;

            for (Map.Entry<String, String> entry : entries) {
                if (Locale.getDefault().getLanguage().equals(entry.getKey())) {
                    name = entry.getValue();
                    break;
                }
            }
            return name == null ? module.id : name;
        }
    }
}
