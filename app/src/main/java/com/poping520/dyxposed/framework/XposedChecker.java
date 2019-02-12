package com.poping520.dyxposed.framework;

import com.poping520.dyxposed.BuildConfig;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.*;

/**
 * @author WangKZ
 * @version 1.0.0
 * create on 2018/11/8 17:23
 */
public class XposedChecker implements IXposedHookLoadPackage {

    /**
     * {@link BaseMainActivity#waitHook()}
     */
    private static final String METHOD_NAME = "waitHook";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (BuildConfig.APPLICATION_ID.equals(lpparam.packageName)) {
            final Class<?> clz = findClass(BaseMainActivity.class.getName(), lpparam.classLoader);

            findAndHookMethod(clz, METHOD_NAME, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);

                    param.setResult(false);
                }
            });
        }
    }
}
