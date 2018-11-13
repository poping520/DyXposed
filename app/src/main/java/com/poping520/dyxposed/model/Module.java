package com.poping520.dyxposed.model;

import android.support.annotation.Nullable;

import com.poping520.dyxposed.annotation.MustParam;

/**
 * Created by WangKZ on 18/11/07.
 *
 * @author poping520
 * @version 1.0.0
 */
public class Module {

    /**
     * 模块名称
     */
    @MustParam
    public String name;

    /**
     * 模块简述
     */
    @Nullable
    public String description;

    /**
     * 指定要 Hook 应用
     */
    @Nullable
    public String[] targetApp;

    /**
     * 模块版本号
     */
    @Nullable
    public String version;
}
