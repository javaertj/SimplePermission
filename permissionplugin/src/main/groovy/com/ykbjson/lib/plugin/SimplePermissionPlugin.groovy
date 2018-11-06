package com.ykbjson.lib.plugin

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

public class SimplePermissionPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        System.out.println("======================================================")
        System.out.println("=    welcome to simple permission gradle plugin!     =")
        System.out.println("======================================================")
        //注册SimplePermissionTransform
        def android = project.extensions.findByType(AppExtension)
        def classTransform = new SimplePermissionTransform(project)
        android.registerTransform(classTransform)
    }
}