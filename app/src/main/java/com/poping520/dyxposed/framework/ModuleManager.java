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
import com.poping520.dyxposed.util.FileUtil;

import java.io.IOException;


/**
 * @author WangKZ
 * @version 1.0.0
 * create on 2018/11/12 16:03
 */
public class ModuleManager {

    private static final String TAG = "ModuleManager";

    private ModuleDBHelper mDBHelper;

    public ModuleManager() {
        String dbName = DyXContext.APPLICATION_NAME + ".db";
        mDBHelper = ModuleDBHelper.getInstance();
    }

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

    @MainThread
    public void parseModule(ModuleSource src) {

        new Thread(() -> {
            final Result<String> compileRet = DyXCompiler.compile(src.path);

            if (compileRet.succ) {
                String dexPath = compileRet.obj;
                final Result<Module> processRet = AnnotationProcessor.process(dexPath);

                if (processRet.succ) {
                    final Module module = processRet.obj;
                    Log.e(TAG, "parseModule: " + module);

                    // 查询模块是否已存在
                    final Module last = mDBHelper.query(module.id);
                    if (last == null) {
                        try {
                            mDBHelper.insert(module, FileUtil.readBytes(dexPath, false));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {

                    }
                }
            }
        }).start();
    }
}
