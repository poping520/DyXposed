package com.poping520.dyxposed.framework;

import com.poping520.dyxposed.exception.DyXRuntimeException;
import com.poping520.dyxposed.model.Library;
import com.poping520.dyxposed.util.FileUtil;

import java.io.File;
import java.io.IOException;

/**
 * Assets Library 文件管理
 *
 * @author WangKZ
 * @version 1.0.0
 * create on 2018/12/6 10:57
 */
public enum LibraryAssets {

    API_ANDROID("api/api-android-28.jar", Library.Scope.BOOT_RT),

    // /data/data/com.poping520.dyxposed/files/api/api-xposed-82.jar
    API_XPOSED("api/api-xposed-82.jar", Library.Scope.DYXPOSED_COMPILE_ONLY),

    API_DYXPOSED("api/api-dyxposed-2.jar", Library.Scope.DYXPOSED_COMPILE_ONLY),

    LIB_XPOSED("lib/lib-xposed.jar", Library.Scope.DYXPOSED_RUNTIME);

    private String assetPath;
    private File file;
    private Library.Scope scope;

    /**
     * @param assetPath assets 路径
     */
    LibraryAssets(String assetPath, Library.Scope scope) {
        this.assetPath = assetPath;
        this.scope = scope;
        file = new File(Env.COMPILE_ENV, assetPath);
    }

    /**
     * 释放 LibraryAssets 资源文件到 WORK DIR
     *
     * <p>
     * save path = WORK DIR + assetPath
     *
     * @return 保存路径
     */
    String release() {
        try {
            return release(Env.COMPILE_ENV);
        } catch (IOException e) {
            e.printStackTrace();
            throw new DyXRuntimeException("...");
        }
    }

    /**
     * 释放 LibraryAssets 资源到指定文件夹下
     *
     * @param dir 指定的文件夹路径
     * @return 输出文件绝对路径
     */
    public String release(String dir) throws IOException {
        final File file = new File(dir, assetPath);
        final String absPath = file.getAbsolutePath();
        if (!file.exists()) {
            FileUtil.unZipAsset(DyXContext.getApplicationContext(), assetPath, absPath, true);
        }
        return absPath;
    }

    /**
     * @return File 对象
     */
    File getFile() {
        return file;
    }

    /**
     * @return 编译方式
     * @see Library.Scope
     */
    public Library.Scope getScope() {
        return scope;
    }

    /**
     * 生成 {@link Library} 对象
     */
    Library generateLibrary() {
        final Library library = new Library(getScope(), getFile().getAbsolutePath(), true);
        library.assets = this;
        return library;
    }
}
