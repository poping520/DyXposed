package com.poping520.dyxposed.framework;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.MainThread;
import android.util.Log;

import com.poping520.dyxposed.api.DyXEntryClass;
import com.poping520.dyxposed.api.DyXEntryMethod;
import com.poping520.dyxposed.api.DyXModuleDesc;
import com.poping520.dyxposed.api.DyXModuleName;
import com.poping520.dyxposed.api.DyXModuleVer;
import com.poping520.dyxposed.api.DyXTargetApp;
import com.poping520.dyxposed.api.ElementName;
import com.poping520.dyxposed.model.Module;
import com.poping520.dyxposed.model.ModuleSource;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Enumeration;

import dalvik.system.DelegateLastClassLoader;
import dalvik.system.DexClassLoader;
import dalvik.system.DexFile;
import dalvik.system.PathClassLoader;

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
            final DyXCompiler.Result ret = DyXCompiler.compile(src.path);

            if (ret.success) {
                parseModule0(ret.dexPath);
            }

        }).start();
    }


    private void parseModule0(String dexPath) {
        Class<?> entryClz = null;

        try {
            final DexFile dexFile = new DexFile(dexPath);
            final Enumeration<String> entries = dexFile.entries();

            final ClassLoader cl = getClass().getClassLoader();
            while (entries.hasMoreElements()) {
                final Class clz = dexFile.loadClass(entries.nextElement(), cl);
                if (clz.isAnnotationPresent(DyXEntryClass.class)) {
                    entryClz = clz;
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (entryClz == null) {
            mHandler.sendEmptyMessage(1);
            return;
        }

        final Module module = new Module();
        final Class<Module> moduleClz = Module.class;

        final Class<? extends Annotation>[] annotations = new Class[]{
                DyXEntryClass.class,
                DyXEntryMethod.class,
                DyXModuleDesc.class,
                DyXModuleName.class,
                DyXModuleVer.class,
                DyXTargetApp.class
        };
        final Field[] fields = entryClz.getDeclaredFields();
        try {
            final Constructor<?> entryClzConstructor = entryClz.getDeclaredConstructor();
            entryClzConstructor.setAccessible(true);
            final Object obj = entryClzConstructor.newInstance();

            Log.e(TAG, "annotations.length: " + annotations.length);

            for (Class<? extends Annotation> annotation : annotations) {

                if (!annotation.isAnnotationPresent(ElementName.class)) {
                    continue;
                }
                final ElementName element = annotation.getAnnotation(ElementName.class);

                for (Field field : fields) {
                    field.setAccessible(true);

                    if (field.isAnnotationPresent(annotation)) {
                        Log.e(TAG, "parseModule0: ");
                        final Field moduleField = moduleClz.getDeclaredField(element.value());
                        moduleField.setAccessible(true);
                        moduleField.set(module, field.get(obj));
                    }
                }
            }

            Log.e(TAG, "parseModule0: " + module);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
