package com.poping520.dyxposed.model;

import android.support.annotation.Nullable;

import com.poping520.dyxposed.R;
import com.poping520.dyxposed.annotation.MustNonEmpty;

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
     * 模块唯一标识
     */
    @MustNonEmpty(R.string.module_field_id)
    public String id;

    /**
     * 模块作者
     */
    @Nullable
    public String author;

    /**
     * 模块名称
     * 转成 JSONObject 字符串保存
     */
    @Nullable
    public Map<String, String> name;

    /**
     * 模块描述
     * 转成 JSONObject 字符串保存
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
     * 转成 JSONArray 字符串保存
     */
    @Nullable
    public String[] target;

    /**
     * 入口类 类名
     */
    @MustNonEmpty(R.string.module_field_entry_class)
    public String entryClass;

    /**
     * 入口 方法名
     */
    @MustNonEmpty(R.string.module_field_entry_method)
    public String entryMethod;

    /**
     * 开启状态
     */
    public boolean enable;

    @Override
    public String toString() {
        return "Module{" +
                "id='" + id + '\'' +
                ", author='" + author + '\'' +
                ", name=" + name +
                ", desc=" + desc +
                ", version='" + version + '\'' +
                ", target=" + Arrays.toString(target) +
                ", entryClass='" + entryClass + '\'' +
                ", entryMethod='" + entryMethod + '\'' +
                ", enable=" + enable +
                '}';
    }
}
