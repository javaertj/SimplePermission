package com.ykbjson.lib.simplepermission.ano;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Desription：onRequestPermissionsResult方法回调的接收者
 * Creator：yankebin
 * CreatedAt：2018/10/29
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PermissionNotify {
}
