package com.poping520.dyxposed.model;

import android.support.annotation.Nullable;

import com.poping520.dyxposed.annotation.MustParam;

import java.util.Arrays;
import java.util.Map;

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
    public Map<String, String> name;

    /**
     * 模块简述
     */
    @Nullable
    public Map<String, String> desc;

    /**
     * 模块版本号
     */
    @Nullable
    public String version;

    /**
     * 指定要 Hook 应用
     */
    @Nullable
    public String[] target;

    /**
     * dex 文件路径
     */
    @MustParam
    public String dexPath;

    /**
     * 入口类 类名
     */
    @MustParam
    public String entryClassName;

    /**
     * 入口 方法名
     */
    @MustParam
    public String entryMethodName;

    @Override
    public String toString() {
        return "Module{" +
                "name=" + name +
                ", desc=" + desc +
                ", version='" + version + '\'' +
                ", target=" + Arrays.toString(target) +
                ", dexPath='" + dexPath + '\'' +
                ", entryClassName='" + entryClassName + '\'' +
                ", entryMethodName='" + entryMethodName + '\'' +
                '}';
    }
}
