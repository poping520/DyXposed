package com.poping520.dyxposed.framework;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.MainThread;

import com.poping520.dyxposed.model.ModuleSource;

import dalvik.system.DexClassLoader;

/**
 * @author WangKZ
 * @version 1.0.0
 * create on 2018/11/12 16:03
 */
public class ModuleManager {

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

    @MainThread
    public void parseModule(ModuleSource src) {

        new Thread(() -> {
            final DyXCompiler.Result ret = DyXCompiler.compile(src.path);

            if (ret.success) {
                parseModule0(ret.dexPath);
            }

        }).start();
    }


    private void parseModule0(String dexPath) {
        final String opDir = DyXContext
                .getApplicationContext()
                .getDir("module", Context.MODE_PRIVATE)
                .getAbsolutePath();

        final DexClassLoader cl =
                new DexClassLoader(dexPath, opDir, null, null);


    }
}
