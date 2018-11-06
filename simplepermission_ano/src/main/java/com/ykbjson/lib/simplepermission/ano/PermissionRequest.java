package com.ykbjson.lib.simplepermission.ano;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Desription：请求权限的方法注解
 * Creator：yankebin
 * CreatedAt：2018/10/29
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PermissionRequest {

    /**
     * 申请权限的code，在权限回调的时候可以根据code知道哪些权限被授予或拒绝
     */
    int requestCode() default 0;

    /**
     * 要申请的权限数组
     */
    String[] requestPermissions() default {""};

    /**
     * 是否需要在申请成功后再次执行代码逻辑
     */
    boolean needReCall() default false;
}
