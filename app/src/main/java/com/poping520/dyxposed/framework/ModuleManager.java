package com.poping520.dyxposed.framework;

import android.content.Context;

import java.io.File;

import dalvik.system.DexClassLoader;

/**
 * @author WangKZ
 * @version 1.0.0
 * create on 2018/11/12 16:03
 */
public class ModuleManager {

    public void loadModule(String dexPath) {
        final Context context = DyXContext.getApplicationContext();

        final File dir = context.getDir("module", Context.MODE_PRIVATE);

        final DexClassLoader classLoader =
                new DexClassLoader(dexPath, dir.getAbsolutePath(), null, ClassLoader.getSystemClassLoader());
    }
}
