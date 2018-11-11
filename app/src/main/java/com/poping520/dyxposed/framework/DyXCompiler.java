package com.poping520.dyxposed.framework;

import android.support.annotation.Nullable;
import android.util.Log;

import com.poping520.dyxposed.exception.DyXRuntimeException;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.tools.FileObject;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

/**
 * Created by WangKZ on 18/11/11.
 *
 * @author poping520
 * @version 1.0.0
 */
public class DyXCompiler {

    private static final String TAG = "DyXCompiler";

    /**
     * 将 java 源码编译为 dex 文件
     *
     * @return
     */
    public static boolean compile() {

        final JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
        final StandardJavaFileManager fm =
                javac.getStandardFileManager(null, Locale.getDefault(), Charset.forName("UTF-8"));

        fm.setBootClassJarPath(Env.Api.ANDROID_RT.getWorkPath());

        try {
            fm.setLocation(StandardLocation.CLASS_PATH,
                    Collections.singletonList(new File(Env.Api.XPOSED_API.getWorkPath())));
            fm.setLocation(StandardLocation.CLASS_OUTPUT,
                    Collections.singletonList(new File("/sdcard/DyXposed/classes")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        final Iterable<? extends JavaFileObject> javaFileObjects =
                fm.getJavaFileObjects(new File("/sdcard/DyXposed/com/poping520/Test.java"));


        StringWriter sw = new StringWriter();
        List<String> op = new ArrayList<>();
        op.add("-verbose");
        javac.getTask(sw, fm, null, op, null, javaFileObjects).call();

        Log.e(TAG, "compile: " + sw.toString());

        final List<File> files = listJavaSrcFile(null, "/sdcard/DyXposed");
        for (File file : files) {
            Log.e(TAG, "compile: " + file.getAbsolutePath());
        }
        return false;
    }

    /**
     * 遍历目录下所有 java 源码文件
     *
     * @param srcPath
     * @return
     */
    private static List<File> listJavaSrcFile(@Nullable List<File> list, String srcPath) {
        boolean isRootPath = false;
        if (list == null) {
            isRootPath = true;
            list = new ArrayList<>();
        }

        final File srcDir = new File(srcPath);

        if (isRootPath && srcDir.isFile()) {
            throw new DyXRuntimeException();
        }

        final File[] files = srcDir.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".java")) {
                    list.add(file);
                } else if (file.isDirectory()) {
                    listJavaSrcFile(list, file.getAbsolutePath());
                }
            }
        }

        return list;
    }
}
