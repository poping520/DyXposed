package com.poping520.dyxposed.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 修饰 保存模块简述 的变量
 *
 * @author WangKZ
 * @version 1.0.0
 * create on 2018/11/13 17:20
 */
@DyXApiElement("desc")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DyXModuleDesc {
}
