package com.poping520.dyxposed.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 修饰 DyXposed 的入口类
 *
 * @author WangKZ
 * @version 1.0.0
 * create on 2018/11/13
 */
@DyXApiElement("entryClass")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DyXEntryClass {

    String id();

    String author() default "";

    String version() default "1.0.0";
}
