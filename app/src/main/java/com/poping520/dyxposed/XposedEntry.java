package com.poping520.dyxposed;

import android.annotation.SuppressLint;
import android.os.Process;

import com.poping520.dyxposed.forcestop.TargetSerializer;
import com.poping520.dyxposed.forcestop.Targets;
import com.poping520.dyxposed.os.AndroidOS;
import com.poping520.dyxposed.os.Shell;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import dalvik.system.PathClassLoader;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by WangKZ on 18/11/07.
 *
 * @author poping520
 * @version 1.0.0
 */
public class XposedEntry implements IXposedHookLoadPackage {

    private static final String MODULE_DIR = "module";
    private static final String DYXPOSED_LIB_PATH = "/lib/lib-dyxposed.jar";

    @SuppressLint("SdCardPath")
    private static final String[] WORK_DIRS = {
            "/sdcard/DyXposed",
            "/data/data/com.poping520.dyxposed/shared"
    };

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        for (String workDir : WORK_DIRS) {
            final File moduleDir = new File(workDir, MODULE_DIR);
            final File[] files = moduleDir.listFiles(
                    pathname -> pathname.getName().endsWith(".json"));

            if (files == null) {
                continue;
            }

            for (File file : files) {
                if (file.exists() && file.canRead()) {
                    onProxy(file, lpparam);
                }
            }
        }
    }

    private void onProxy(File jsonFile, XC_LoadPackage.LoadPackageParam lpparam) {
        final File moduleDir = jsonFile.getParentFile();
        final String moduleBaseName = jsonFile.getName().replaceAll(".json", "");
        // 模块 jar 文件
        final String dexName = moduleBaseName + ".jar";

        final File dexFile = new File(moduleDir, dexName);
        if (!dexFile.exists() || !dexFile.canRead()) {
            return;
        }

        try {
            final JSONObject jObj = new JSONObject(readString(jsonFile));
            final List<String> target = getTarget(jObj.optJSONArray("target"));
            final String entryClass = jObj.optString("entryClass");
            final String entryMethod = jObj.optString("entryMethod");

            if (target.isEmpty() || target.contains(lpparam.packageName)) {
                execProxy(dexFile.getAbsolutePath(), entryClass, entryMethod, lpparam);

                if (target.isEmpty()) {
                    return;
                }

                /* TEST ONLY */
                final String targetsName = moduleBaseName + ".targets";
                final File targetsFile = new File(moduleDir, targetsName);

                Targets targets;
                if (targetsFile.exists()) {
                    targets = TargetSerializer.deserialize(targetsFile);
                } else {
                    targets = new Targets();
                }

                if (targets != null) {
                    final Targets.Target t = targets.targetMap.get(lpparam.packageName);

                    final String currentProcessName = AndroidOS.getCurrentProcessName();
                    if (t == null) {
                        final Targets.Target tmp = new Targets.Target();
                        tmp.pidMap.put(currentProcessName, Process.myPid());
                        targets.targetMap.put(lpparam.packageName, tmp);
                    } else {
                        t.pidMap.remove(currentProcessName);
                        t.pidMap.put(currentProcessName, Process.myPid());
                    }

                    TargetSerializer.serialize(targetsFile, targets);

                    String cmd1 = "chmod 777 " + targetsFile.getAbsolutePath();
                    String cmd2 = "chown 9997:9997 " + targetsFile.getAbsolutePath();

                    try {
                        Runtime.getRuntime().exec(new String[]{cmd1, cmd2});
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                /* TEST ONLY */

            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* 执行模块 Xposed 入口方法 */
    private void execProxy(String dexPath, String entryClass,
                           String entryMethod, XC_LoadPackage.LoadPackageParam lpparam) {

        final String dyxposedLibPath = getDyxposedLibPath(dexPath);
        final PathClassLoader parent =
                new PathClassLoader(dyxposedLibPath, null, XposedBridge.class.getClassLoader());
        final PathClassLoader cl = new PathClassLoader(dexPath, null, parent);

        try {
            final Class<?> proxyClz = cl.loadClass(entryClass);
            final Constructor<?> constructor = proxyClz.getDeclaredConstructor();
            constructor.setAccessible(true);
            final Object proxyObj = constructor.newInstance();
            final Method proxyMethod = proxyClz.getDeclaredMethod(entryMethod, XC_LoadPackage.LoadPackageParam.class);
            proxyMethod.setAccessible(true);
            proxyMethod.invoke(proxyObj, lpparam);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getDyxposedLibPath(String dexPath) {
        if (dexPath.contains(WORK_DIRS[1]))
            return WORK_DIRS[1] + DYXPOSED_LIB_PATH;
        else
            return WORK_DIRS[0] + DYXPOSED_LIB_PATH;
    }

    private List<String> getTarget(JSONArray jArr) {
        List<String> list = new ArrayList<>();
        if (jArr != null) {
            final int length = jArr.length();
            for (int i = 0; i < length; i++) {
                list.add(jArr.optString(i));
            }
        }
        return list;
    }

    private String readString(File file) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(file));
        StringBuilder sb = new StringBuilder();
        String len;
        while ((len = br.readLine()) != null) {
            sb.append(len).append("\n");
        }
        return sb.toString();
    }
}
