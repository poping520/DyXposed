package com.poping520.dyxposed.model;

import com.poping520.dyxposed.framework.LibraryAssets;

import java.io.File;

/**
 * @author WangKZ
 * @version 1.0.0
 * create on 2018/12/5 17:08
 */
public class Library {

    public Scope scope;

    public String path;

    public boolean enable;

    public LibraryAssets assets;

    public Library(Scope scope, String path, boolean enable) {
        this.scope = scope;
        this.path = path;
        this.enable = enable;
    }

    public boolean isDyXLibrary() {
        return assets != null;
    }

    public String getName() {
        return new File(path).getName();
    }

    /**
     * Lib 库的编译方式
     */
    public enum Scope {

        /**
         * BOOT CLASS
         * <li>api-android-28.jar {@link LibraryAssets#API_ANDROID}</li>
         */
        BOOT_RT,

        /**
         * DyXposed 编译时依赖
         * <li>api-xposed-82.jar {@link LibraryAssets#API_XPOSED}</li>
         * <li>api-dyxposed-2.jar {@link LibraryAssets#API_DYXPOSED}</li>
         */
        DYXPOSED_COMPILE_ONLY,

        /**
         * DyXposed 运行时依赖
         * <li>lib/lib-xposed.jar (dex) {@link LibraryAssets#LIB_XPOSED}</li>
         * <li>lib-dyxposed.jar (dex)</li>
         */
        DYXPOSED_RUNTIME,

        /**
         * 用户 编译时依赖 不编译进模块
         */
        USER_COMPILE_ONLY,

        /**
         * 用户 运行时依赖 编译进模块
         */
        USER_RUNTIME
    }
}
