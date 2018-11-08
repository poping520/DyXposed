package com.poping520.dyxposed.framework;

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
     * 模块的全类名
     */
    @MustParam
    public String proxyClassName;

    /**
     * 是否启用
     */
    public boolean enable;

    /**
     * 模块简述
     */
    @Nullable
    public String description;

    /**
     * 指定要 Hook 应用的包名
     */
    @Nullable
    public String hookPackageName;

    /**
     * 模块版本号
     */
    @Nullable
    public String version;
}
