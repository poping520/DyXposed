package com.poping520.dyxposed.api;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.poping520.dyxposed.R;
import com.poping520.dyxposed.annotation.MustNonEmpty;
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
import java.util.Collection;
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
            DyXTargetApp.class,
    };

    /**
     * {@link MustNonEmpty} 注解处理, 校验类对象合法性
     *
     * @param obj 要处理的类对象
     * @return 返回集合装有非法成员变量的变量名或显示名称, 即集合为空, 对象合法
     * @see MustNonEmpty
     */
    @NonNull
    public static List<String> processMustNonEmpty(Object obj) {
        if (obj == null)
            throw new IllegalArgumentException();

        List<String> list = new ArrayList<>();
        final Field[] fields = obj.getClass().getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(MustNonEmpty.class)) {
                Object value = null;
                try {
                    value = field.get(obj);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

                if (value != null) {
                    if (value instanceof CharSequence && ((CharSequence) value).length() == 0) {
                        value = null;
                    } else if (value instanceof Collection && ((Collection) value).size() == 0) {
                        value = null;
                    }
                }

                if (value == null) {
                    int stringResId = field.getAnnotation(MustNonEmpty.class).value();
                    String showName = stringResId == 0
                            ? field.getName()
                            : DyXContext.getStringFromRes(stringResId);
                    if (TextUtils.isEmpty(showName))
                        showName = field.getName();

                    list.add(showName);
                }
            }
        }
        return list;
    }

    // 处理模块 dex 文件
    public static Result<Module> processDyXApi(@NonNull String moduleDexPath) {
        Result<Module> ret = new Result<>();

        final Module moduleClzObj = new Module();
        final Class<Module> moduleClz = Module.class;

        // 代理/入口类确认
        Class<?> entryClz = findEntryClass(moduleDexPath);
        if (entryClz == null) {
            ret.errMsg = DyXContext.getStringFromRes(R.string.process_err_entry_clz_not_find);
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

        final DyXEntryClass anno = entryClz.getAnnotation(DyXEntryClass.class);
        moduleClzObj.id = anno.id();
        moduleClzObj.version = anno.version();
        moduleClzObj.author = anno.author();

        // 代理/入口方法确认
        final Method entryMethod = findEntryMethod(entryClz);
        if (entryMethod == null) {
            ret.errMsg = DyXContext.getStringFromRes(R.string.process_err_entry_method_not_find);
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
            ret.errMsg = DyXContext.getStringFromRes(R.string.process_err_entry_method_not_find);
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

        List<String> nulls = processMustNonEmpty(moduleClzObj);
        if (nulls.isEmpty()) {
            ret.succ = true;
            ret.obj = moduleClzObj;
        } else {
            StringBuilder sb = new StringBuilder();
            for (String str : nulls) {
                sb.append(str).append(" ");
            }
            ret.errMsg = DyXContext.getStringFromRes(R.string.process_err_module_lack_element, sb.toString());
        }
        return ret;
    }

    // 代理/入口类确认
    @Nullable
    private static Class<?> findEntryClass(@NonNull String dexPath) {
        Class<?> entryClz = null;
        try {
            // API 26 DexFile Deprecated
            final DexFile dexFile = new DexFile(dexPath);
            final Enumeration<String> entries = dexFile.entries();

            final ClassLoader moduleClassLoader = Env.getInstance().getDyXModuleClassLoader();
            while (entries.hasMoreElements()) {
                final Class clz = dexFile.loadClass(
                        entries.nextElement(),
                        moduleClassLoader
                );
                if (clz != null && clz.isAnnotationPresent(DyXEntryClass.class)) {
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
                // 方法参数校验
                final Class<?>[] parameterTypes = method.getParameterTypes();
                try {
                    if ("de.robv.android.xposed.callbacks.XC_LoadPackage$LoadPackageParam"
                            .equals(parameterTypes[0].getName())) {
                        entryMethod = method;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
        }
        return entryMethod;
    }

    // 获取 Module 成员变量名
    private static String getModuleFieldName(Class<? extends Annotation> clz) {
        if (!clz.isAnnotationPresent(DyXApiElement.class))
            throw new IllegalArgumentException();
        return clz.getAnnotation(DyXApiElement.class).value();
    }
}
