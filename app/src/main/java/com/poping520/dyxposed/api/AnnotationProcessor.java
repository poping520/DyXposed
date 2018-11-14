package com.poping520.dyxposed.api;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.poping520.dyxposed.R;
import com.poping520.dyxposed.annotation.MustNonNull;
import com.poping520.dyxposed.framework.DyXContext;
import com.poping520.dyxposed.framework.Env;
import com.poping520.dyxposed.model.Result;
import com.poping520.dyxposed.model.Module;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import dalvik.system.DexFile;

/**
 * @author WangKZ
 * @version 1.0.0
 * create on 2018/11/14 10:27
 */
public final class AnnotationProcessor {

    @SuppressWarnings("unchecked")
    private static final Class<? extends Annotation>[] API_ANNOTATION_CLASSES = new Class[]{
            DyXModuleName.class,
            DyXModuleDesc.class,
            DyXModuleVer.class,
            DyXTargetApp.class,
    };

    // 处理模块 dex 文件
    public static Result<Module> process(@NonNull String moduleDexPath) {
        Result<Module> ret = new Result<>();

        final Module moduleClzObj = new Module();
        final Class<Module> moduleClz = Module.class;
        moduleClzObj.dexPath = moduleDexPath;

        // 代理/入口类确认
        Class<?> entryClz = findEntryClass(moduleDexPath);
        if (entryClz == null) {
            ret.errMsg = DyXContext.getString(R.string.process_err_entry_clz_not_find);
            return ret;
        }
        try {
            moduleClz
                    .getField(getModuleFieldName(DyXEntryClass.class))
                    .set(moduleClzObj, entryClz.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 代理/入口类确认


        // 代理/入口方法确认
        final Method entryMethod = findEntryMethod(entryClz);
        if (entryMethod == null) {
            ret.errMsg = DyXContext.getString(R.string.process_err_entry_method_not_find);
            return ret;
        }
        try {
            moduleClz
                    .getField(getModuleFieldName(DyXEntryMethod.class))
                    .set(moduleClzObj, entryMethod.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 代理/入口方法确认


        // 创建代理/入口类 对象
        Object entryClzObj = null;
        try {
            final Constructor<?> entryClzConstructor = entryClz.getDeclaredConstructor();
            entryClzConstructor.setAccessible(true);
            entryClzObj = entryClzConstructor.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (entryClzObj == null) {
            ret.errMsg = DyXContext.getString(R.string.process_err_entry_method_not_find);
            return ret;
        }
        // 创建代理/入口类 对象


        // 获取代理/入口类成员变量的值 赋给 Module 对象
        final Field[] entryClzFields = entryClz.getDeclaredFields();
        for (Class<? extends Annotation> annClz : API_ANNOTATION_CLASSES) {

            for (Field entryClzField : entryClzFields) {
                entryClzField.setAccessible(true);

                if (entryClzField.isAnnotationPresent(annClz)) {
                    try {
                        moduleClz
                                .getField(getModuleFieldName(annClz))
                                .set(moduleClzObj, entryClzField.get(entryClzObj));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        // 获取代理/入口类成员变量的值 赋给 Module 对象

        List<String> nulls = processMustNonNull(moduleClzObj);
        if (nulls.size() == 0) {
            ret.succ = true;
            ret.obj = moduleClzObj;
        } else {
            StringBuilder sb = new StringBuilder();
            for (String str : nulls) {
                sb.append(str).append(" ");
            }
            ret.errMsg = DyXContext.getString(R.string.process_err_module_lack_element, sb.toString());
        }
        return ret;
    }

    /**
     * {@link MustNonNull} 注解处理
     *
     * @param obj 要处理的类对象
     * @return 装有成员变量的值为 NULL 的显示名称的集合
     */
    @NonNull
    public static List<String> processMustNonNull(Object obj) {
        if (obj == null)
            throw new IllegalArgumentException();

        List<String> list = new ArrayList<>();
        final Field[] fields = obj.getClass().getFields();

        for (Field field : fields) {
            if (field.isAnnotationPresent(MustNonNull.class)) {
                Object value = null;
                try {
                    value = field.get(obj);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

                if (value == null) {
                    final MustNonNull ann = field.getAnnotation(MustNonNull.class);
                    int stringResId = ann.value();
                    String showName = stringResId
                            == 0
                            ? field.getName()
                            : DyXContext.getString(stringResId);
                    list.add(showName);
                }
            }
        }
        return list;
    }

    // 代理/入口类确认
    @Nullable
    private static Class<?> findEntryClass(@NonNull String dexPath) {
        Class<?> entryClz = null;
        try {
            // API 26 DexFile Deprecated
            final DexFile dexFile = new DexFile(dexPath);
            final Enumeration<String> entries = dexFile.entries();

            while (entries.hasMoreElements()) {
                final Class clz = dexFile.loadClass(entries.nextElement(), Env.getInstance().getDyXModuleClassLoader());
                if (clz.isAnnotationPresent(DyXEntryClass.class)) {
                    entryClz = clz;
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return entryClz;
    }

    // 代理/入口方法确认
    @Nullable
    private static Method findEntryMethod(@NonNull Class<?> entryClz) {
        Method entryMethod = null;
        final Method[] entryClzMethods = entryClz.getDeclaredMethods();
        for (Method method : entryClzMethods) {
            method.setAccessible(true);
            if (method.isAnnotationPresent(DyXEntryMethod.class)) {
                entryMethod = method;
                break;
            }
        }
        return entryMethod;
    }

    // 获取 Module 成员变量名
    private static String getModuleFieldName(Class<? extends Annotation> clz) {
        if (!clz.isAnnotationPresent(DyXApiElement.class))
            throw new IllegalArgumentException();
        final DyXApiElement element = clz.getAnnotation(DyXApiElement.class);
        return element.value();
    }
}
