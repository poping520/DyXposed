package com.poping520.dyxposed.framework;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.MainThread;
import android.util.Log;

import com.poping520.dyxposed.api.AnnotationProcessor;
import com.poping520.dyxposed.model.Module;
import com.poping520.dyxposed.model.ModuleSource;
import com.poping520.dyxposed.model.Result;


/**
 * @author WangKZ
 * @version 1.0.0
 * create on 2018/11/12 16:03
 */
public class ModuleManager {

    private static final String TAG = "ModuleManager";

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

    @MainThread
    public void parseModule(ModuleSource src) {

        new Thread(() -> {
            final Result<String> compRet = DyXCompiler.compile(src.path);

            if (compRet.succ) {
                final Result<Module> processRet = AnnotationProcessor.process(compRet.obj);

                if (processRet.succ) {
                    final Module module = processRet.obj;
                    Log.e(TAG, "parseModule: " + module);
                }
            }
        }).start();
    }
}
