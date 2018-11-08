package com.poping520.dyxposed;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * @author WangKZ
 * @version 1.0.0
 * create on 2018/11/8 17:13
 */
public interface IXposedEntryProxy {

    void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable;
}
