package com.poping520.dyxposed.model;

/**
 * @author WangKZ
 * @version 1.0.0
 * create on 2018/12/5 17:08
 */
public class Library {

    public String name;

    public String path;

    public Scope scope;

    public boolean enable;

    public Library(String name, String path, Scope scope, boolean enable) {
        this.name = name;
        this.path = path;
        this.scope = scope;
        this.enable = enable;
    }

    public enum Scope {

        /**
         * BOOT CLASS
         */
        BOOT_RT,

        /**
         * DyXposed 编译时依赖
         */
        DYXPOSED_COMPILE_ONLY,

        /**
         * DyXposed 运行时依赖
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
