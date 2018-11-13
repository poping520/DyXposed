package com.poping520.dyxposed.framework;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.poping520.dyxposed.exception.DyXRuntimeException;
import com.poping520.dyxposed.libdx.DxTool;
import com.poping520.dyxposed.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

/**
 * DyXposed 编译器
 * <p>
 * Created by WangKZ on 18/11/11.
 *
 * @author poping520
 * @version 1.0.0
 */
public final class DyXCompiler {

    private static final String TAG = "DyXCompiler";

    // java => class => dex
    public static Result compile(String srcPath) {
        final Result result = new Result();
        result.success = false;

        @NonNull final List<File> javaFiles = listJavaSrcFile(srcPath);

        if (javaFiles.size() <= 0) {
            result.errMsg = "该目录没有 java 源码文件";
            return result;
        }

        final File classOutputDir = new File(Env.getClassOutputDir());

        final JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> collector = new DiagnosticCollector<>();

        final StandardJavaFileManager fm =
                javac.getStandardFileManager(collector, Locale.getDefault(), Charset.forName("UTF-8"));

        fm.setBootClassJarPath(Env.Api.ANDROID_RT.getWorkPath());

        try {
            List<File> api = new ArrayList<>();
            Collections.addAll(
                    api,
                    new File(Env.Api.XPOSED_API.getWorkPath()),
                    new File(Env.Api.DYXPOSED_API.getWorkPath())
            );
            fm.setLocation(StandardLocation.CLASS_PATH, api);
            fm.setLocation(StandardLocation.CLASS_OUTPUT, Collections.singletonList(classOutputDir));

            final Iterable<? extends JavaFileObject> compilationUnits =
                    fm.getJavaFileObjectsFromFiles(javaFiles);

            StringWriter sw = new StringWriter();
            List<String> op = new ArrayList<>();
            op.add("-verbose");

            boolean ret = javac.getTask(sw, fm, collector, op, null, compilationUnits).call();

            if (!ret) {
                final List<Diagnostic<? extends JavaFileObject>> diagnostics = collector.getDiagnostics();
                StringBuilder sb = new StringBuilder();
                for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics) {
                    sb.append(String.format("Error on line %s in %s%n", diagnostic.getLineNumber(), diagnostic.getSource().toUri()));
                }
                sb.append("\n").append(sw.toString());
                result.errMsg = sb.toString();
                return result;
            }

            // dx start
            final String dexOutputPath = Env.getInstance().getDexOutputPath();
            final DxTool dxTool = new DxTool.Builder()
                    .inputs(Env.getClassOutputDir())
                    .output(dexOutputPath)
                    .verbose(true)
                    .build();

            ret = dxTool.start();

            if (!ret) {
                //TODO
                result.errMsg = "dx error";
                return result;
            }

            result.success = true;
            result.dexPath = dexOutputPath;
            return result;

        } catch (IOException e) {
            e.printStackTrace();
            result.errMsg = e.toString();
        } finally {
            try {
                fm.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            FileUtil.removeDir(classOutputDir);
        }

        return result;
    }

    static class Result {

        boolean success;

        String dexPath;

        String errMsg;
    }

    /**
     * 遍历目录下所有 java 源码文件
     *
     * @param srcPath src目录
     * @return java文件集合
     */
    @NonNull
    private static List<File> listJavaSrcFile(String srcPath) {
        return listJavaSrcFile(null, srcPath);
    }

    @NonNull
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
