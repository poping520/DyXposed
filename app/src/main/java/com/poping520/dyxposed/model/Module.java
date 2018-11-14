package com.poping520.dyxposed.model;

import android.support.annotation.Nullable;

import com.poping520.dyxposed.R;
import com.poping520.dyxposed.annotation.MustNonNull;
import com.poping520.dyxposed.framework.DyXContext;

import java.util.Arrays;
import java.util.Map;

/**
 * Created by WangKZ on 18/11/07.
 *
 * @author poping520
 * @version 1.0.0
 */
@SuppressWarnings("all")
public class Module {

    /**
     * 模块名称
     */
    @MustNonNull(R.string.module_field_name)
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
    @MustNonNull(R.string.module_field_dex_path)
    public String dexPath;

    /**
     * 入口类 类名
     */
    @MustNonNull(R.string.module_field_entry_class)
    public String entryClass;

    /**
     * 入口 方法名
     */
    @MustNonNull(R.string.module_field_entry_method)
    public String entryMethod;

    /**
     * 开启状态
     */
    public boolean enable;

    @Override
    public String toString() {
        return "Module{" +
                "name=" + name +
                ", desc=" + desc +
                ", version='" + version + '\'' +
                ", target=" + Arrays.toString(target) +
                ", dexPath='" + dexPath + '\'' +
                ", entryClass='" + entryClass + '\'' +
                ", entryMethod='" + entryMethod + '\'' +
                '}';
    }
}
