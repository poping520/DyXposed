package com.poping520.dyxposed.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author WangKZ
 * @version 1.0.0
 * create on 2018/11/15 13:16
 */
@DyXApiElement("author")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DyXModuleAuthor {
}
