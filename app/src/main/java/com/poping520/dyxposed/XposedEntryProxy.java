package com.poping520.dyxposed;

import com.poping520.dyxposed.api.DyXEntryClass;
import com.poping520.dyxposed.api.DyXLoadPackage;
import com.poping520.dyxposed.api.DyXModuleDesc;
import com.poping520.dyxposed.api.DyXModuleName;
import com.poping520.dyxposed.api.DyXModuleVer;
import com.poping520.dyxposed.api.DyXTargetApp;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * @author WangKZ
 * @version 1.0.0
 * create on 2018/11/8 17:13
 */
@DyXEntryClass
public class XposedEntryProxy {

    @DyXModuleName
    String name = "测试模块";

    @DyXModuleName("en")
    String name_en = "test module";

    @DyXModuleDesc
    String desc = "仅供测试";

    @DyXModuleDesc("en")
    String desc_en = "just for test";

    @DyXModuleVer
    String version = "1.0.0";

    @DyXTargetApp
    String target = "com.poping520.dyxposed";

    //or
    @DyXTargetApp
    String[] target_arr = {"com.poping520.dyxposed", "com.android.settings"};


    @DyXLoadPackage
    void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

    }
}
