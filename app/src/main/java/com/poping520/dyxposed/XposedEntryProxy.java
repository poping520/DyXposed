package com.poping520.dyxposed;

import com.poping520.dyxposed.api.DyXEntryClass;
import com.poping520.dyxposed.api.DyXEntryMethod;
import com.poping520.dyxposed.api.DyXModuleAuthor;
import com.poping520.dyxposed.api.DyXModuleDesc;
import com.poping520.dyxposed.api.DyXModuleID;
import com.poping520.dyxposed.api.DyXModuleName;
import com.poping520.dyxposed.api.DyXModuleVer;
import com.poping520.dyxposed.api.DyXTargetApp;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * @author WangKZ
 * @version 1.0.0
 * create on 2018/11/8 17:13
 */
@DyXEntryClass
public class XposedEntryProxy {

    @DyXModuleID
    String id = "dyxposed-test";

    @DyXModuleAuthor
    String author = "biubiubiu";

    @DyXModuleName
    Map<String, String> name = new HashMap<>();

    @DyXModuleDesc
    Map<String, String> desc = new HashMap<>();

    @DyXModuleVer
    String version = "1.0.0";

    @DyXTargetApp
    String[] target_arr = {"com.poping520.dyxposed", "com.android.settings"};

    XposedEntryProxy() {
        name.put("zh", "测试模块");
        name.put("en", "Test Module");

        desc.put(Locale.CHINESE.getLanguage(), "仅供测试");
        desc.put(Locale.ENGLISH.getLanguage(), "just for test");
    }

    @DyXEntryMethod
    void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

    }
}
