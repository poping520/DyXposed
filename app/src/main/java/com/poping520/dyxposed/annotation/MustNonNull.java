package com.poping520.dyxposed.annotation;

import android.support.annotation.StringRes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 修饰 值不能为 NULL 的成员变量
 * <p>
 * Created by WangKZ on 18/11/08.
 *
 * @author poping520
 * @version 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface MustNonNull {
    /**
     * 成员变量 显示名成的资源 id
     * 为 0 则为 变量名
     */
    @StringRes int value() default 0;
}
