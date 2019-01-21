package com.poping520.dyxposed.simple;

import android.text.TextUtils;

import com.poping520.dyxposed.api.*;
import com.poping520.dyxposed.log.DyXLog;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.*;

/**
 * @author WangKZ
 * @version 1.0.0
 * create on 2018/11/20 14:11
 */
@DyXEntryClass(id = "com.dyxposed.module")
public class DyXposedSimpleModule {

    @DyXModuleName
    Map<String, String> name = new HashMap<>();

    @DyXModuleDesc
    Map<String, String> desc = new HashMap<>();

    @DyXTargetApp
    String[] target = {};

    DyXposedSimpleModule() {
        DyXLog.e("SimpleModule","new DyXposedSimpleModule");
        name.put(Locale.CHINESE.getLanguage(), "FFF团");
        desc.put(Locale.CHINESE.getLanguage(), "没有描述");
    }

    @DyXEntryMethod
    void onHook(XC_LoadPackage.LoadPackageParam param) {

        final Class<?> clz = findClass("android.widget.TextView", param.classLoader);
        findAndHookMethod(clz, "setText", CharSequence.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                CharSequence str = (CharSequence) param.args[0];
                if (TextUtils.equals(str, "FFF团")) {
                    return;
                }
                param.args[0] = "FFF";
            }
        });
    }
}
