package com.poping520.dyxposed.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 修饰 DyXposed 的入口方法
 *
 * @author WangKZ
 * @version 1.0.0
 * create on 2018/11/13
 */
@DyXApiElement("entryMethod")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DyXEntryMethod {
}
