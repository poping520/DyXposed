package com.poping520.dyxposed.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author WangKZ
 * @version 1.0.0
 * create on 2018/11/13 17:25
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
@interface DyXApiElement {
    /**
     * @return Module Class 的成员变量名
     */
    String value();
}
