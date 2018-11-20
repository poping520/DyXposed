package com.poping520.dyxposed.annotation;

import android.support.annotation.StringRes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;

/**
 * 本注解用于校验某个类(JavaBean)对象的合法性, 修饰的成员变量需要遵循以下原则：
 * <li>是引用类型且在对象中引用不能为 NULL</li>
 * <li>如果是{@link CharSequence} 类型或子类型, 则对象的{@link CharSequence#length()}不能为 0</li>
 * <li>如果是{@link java.util.Collection} 类型或子类型, 则对象的{@link Collection#size()}不能为 0</li>
 * <p>
 * Created by WangKZ on 18/11/08.
 *
 * @author poping520
 * @version 1.0.0
 * @see com.poping520.dyxposed.api.AnnotationProcessor#processMustNonEmpty(Object)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface MustNonEmpty {

    /**
     * 成员变量(显示名称)的资源 id
     * 为 0 则为 变量名
     */
    @StringRes int value() default 0;
}
