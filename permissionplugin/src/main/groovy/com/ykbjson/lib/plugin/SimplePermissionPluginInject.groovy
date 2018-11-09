package com.ykbjson.lib.plugin

import com.ykbjson.lib.simplepermission.ano.PermissionNotify
import com.ykbjson.lib.simplepermission.ano.PermissionRequest
import javassist.*
import org.gradle.api.Project

public class SimplePermissionPluginInject {

    private static final ClassPool pool = ClassPool.getDefault()

    private static final String PACKAGE_STAT = "com"
    private static final int CLASS_LENGTH = ".class".length() //6
    private static final String INJECT_PERMISSION_NOTIFY_METHOD_NAME = "onRequestPermissionsResult"
    private static
    final String INJECT_PERMISSION_REQUEST_METHOD_PARAMS_FIELD_NAME = "requestPermissionMethodParams"
    private static
    final String PERMISSIONS_MANAGER_PATH = "com.ykbjson.lib.simplepermission.PermissionsManager"
    private static
    final String PERMISSIONS_PATH = "com.ykbjson.lib.simplepermission.Permissions"
    private static
    final String PERMISSIONS_REQUEST_CALLBACK_PATH = "com.ykbjson.lib.simplepermission.PermissionsRequestCallback"
    private static
    final String PERMISSIONS_RESULT_ACTION_PATH = "com.ykbjson.lib.simplepermission.PermissionsResultAction"
    //添加成员变量存储需要请求权限的方法的参数信息
    private static
    final String REQUEST_PERMISSION_METHOD_PARAMS_INJECT_CONTENT = "private final Map requestPermissionMethodParams = new HashMap();"
    private static
    final String PERMISSIONS_REQUEST_CALLBACK_METHOD_ONGRANTED = "public void onGranted(int requestCode,String permission){ }\n"
    private static
    final String PERMISSIONS_REQUEST_CALLBACK_METHOD_ONDENIED = "public void onDenied(int requestCode,String permission){ }\n"
    private static
    final String PERMISSIONS_REQUEST_CALLBACK_METHOD_ONDENIED_FOREVER = "public void onDeniedForever(int requestCode,String permission){ }\n"
    private static
    final String PERMISSIONS_REQUEST_CALLBACK_METHOD_ONFAILURE = "public void onFailure(int requestCode,String[] deniedPermissions){ }\n"
    private static
    final String PERMISSIONS_REQUEST_CALLBACK_METHOD_ONSUCCESS = "public void onSuccess(int requestCode){ }\n"

    private static final String[] PERMISSIONS_REQUEST_CALLBACK_METHODS = [
            PERMISSIONS_REQUEST_CALLBACK_METHOD_ONGRANTED,
            PERMISSIONS_REQUEST_CALLBACK_METHOD_ONDENIED,
            PERMISSIONS_REQUEST_CALLBACK_METHOD_ONDENIED_FOREVER,
            PERMISSIONS_REQUEST_CALLBACK_METHOD_ONFAILURE,
            PERMISSIONS_REQUEST_CALLBACK_METHOD_ONSUCCESS
    ]

    private static
    final String INJECT_PERMISSION_NOTIFY_CONTENT = "PermissionsManager.getInstance().notifyPermissionsChange(permissions,grantResults);\n"

    private static final String ACTIVITY_PATH = "android.app.Activity"

    private static final String FRAGMENT_PATH = "android.app.Fragment"

    private static final String V4_FRAGMENT_PATH = "android.support.v4.app.Fragment"

    static def classPathList = new ArrayList<JarClassPath>()

    public static void removeClassPath(Project project) {
        if (classPathList != null && !classPathList.isEmpty()) {
            classPathList.each {
                try {
                    pool.removeClassPath(it)
                } catch (Exception e) {
                    project.logger.error(e.getMessage())
                }
            }
            classPathList.clear()
        }
        pool.clearImportedPackages()
    }

    public static void injectJar(Project project, String path, String packageName) {
        def classPath = new JarClassPath(path)
        pool.appendClassPath(classPath)
        classPathList.add(classPath)
        //project.android.bootClasspath 加入android.jar，否则找不到android相关的所有类
        pool.appendClassPath(project.android.bootClasspath[0].toString())
    }

    public static void injectDir(Project project, String path, String packageName) {
        //将当前路径加入类池,不然找不到这个类
        pool.appendClassPath(path)
        //project.android.bootClasspath 加入android.jar，不然找不到android相关的所有类
        pool.appendClassPath(project.android.bootClasspath[0].toString())
        //引入com.ykbjson.lib.simplepermission下面要使用到的类
        pool.importPackage(PERMISSIONS_MANAGER_PATH)
        pool.importPackage(PERMISSIONS_PATH)
        pool.importPackage(PERMISSIONS_RESULT_ACTION_PATH)
        pool.importPackage(PERMISSIONS_REQUEST_CALLBACK_PATH)
        pool.importPackage(ACTIVITY_PATH)
        pool.importPackage(FRAGMENT_PATH)
        pool.importPackage(V4_FRAGMENT_PATH)
        pool.importPackage("java.util.Map")
        pool.importPackage("java.util.HashMap")
        pool.importPackage("java.util.List")
        pool.importPackage("java.util.ArrayList")

        File dir = new File(path)
        if (!dir.isDirectory()) {
            return
        }
        dir.eachFileRecurse { File file ->
            String filePath = file.absolutePath
            //确保当前文件是class文件，并且不是系统自动生成的class文件
            if (!filePath.endsWith(".class")
                    || filePath.contains('R$')
                    || filePath.contains('R.class')
                    || filePath.contains("BuildConfig.class")) {
                return
            }
            project.logger.warn "SimplePermission-----> filePath : " + filePath
            int index = filePath.indexOf(PACKAGE_STAT)
            int end = filePath.length() - CLASS_LENGTH // .class = 6
            String className = filePath.substring(index, end)
                    .replace('\\', '.').replace('/', '.')
            project.logger.warn "SimplePermission-----> className : " + className
            if (null == className || "" == className || className.contains("\$")) {
                return
            }
            CtClass c = pool.getCtClass(className)
            //检验注解
            if (!c.hasAnnotation(PermissionNotify.class)) {
                return
            }
            //不是activity或fragment不处理
            if (!c.subclassOf(pool.get(ACTIVITY_PATH)) && !c.subclassOf(pool.get(FRAGMENT_PATH)) && !c.subclassOf(pool.get(V4_FRAGMENT_PATH))) {
                return
            }
            try {
                //找到需要修改的class，开始修改class文件，先解冻
                if (c.isFrozen()) {
                    c.defrost()
                }
                //校验是否实现了PermissionsRequestCallback接口，没有实现则让其实现该接口，并重载相关方法
                if (null == c.getInterfaces() || !c.getInterfaces().contains(pool.get(PERMISSIONS_REQUEST_CALLBACK_PATH))) {
                    c.addInterface(pool.get(PERMISSIONS_REQUEST_CALLBACK_PATH))
                    //重载方法
                    for (String methodStr : PERMISSIONS_REQUEST_CALLBACK_METHODS) {
                        project.logger.warn "SimplePermission-----> override  PermissionsRequestCallback method : " + methodStr
                        CtMethod overrideMethod = CtNewMethod.make(methodStr, c)
                        c.addMethod(overrideMethod)
                    }
                }
                //加入存储方法的map结构
                CtField requestPermissionMethodParamsField = findFieldByName(c, INJECT_PERMISSION_REQUEST_METHOD_PARAMS_FIELD_NAME)
                if (null == requestPermissionMethodParamsField) {
                    requestPermissionMethodParamsField = CtField.make(REQUEST_PERMISSION_METHOD_PARAMS_INJECT_CONTENT, c)
                    c.addField(requestPermissionMethodParamsField)
                }
                //检测Activity或Fragment是否声明或重写了onRequestPermissionsResult方法
                CtMethod notifyMethod = findMethodByName(c, INJECT_PERMISSION_NOTIFY_METHOD_NAME)
                //如果已经重写，则在super之前插入代码
                if (null != notifyMethod) {
                    project.logger.error "SimplePermission-----> find notifyPermission method : " + notifyMethod.longName
                    project.logger.error "SimplePermission-----> notifyPermission method insert content :\n" + INJECT_PERMISSION_NOTIFY_CONTENT
                    notifyMethod.insertBefore(INJECT_PERMISSION_NOTIFY_CONTENT)
                } else {
                    //没有重写，插入重载该方法的代码
                    StringBuilder methodBuilder = new StringBuilder()
                            .append("public void ")
                            .append(INJECT_PERMISSION_NOTIFY_METHOD_NAME)
                            .append("(")
                            .append("int requestCode, ")
                            .append("String[] permissions, ")
                            .append("int[] grantResults) { \n")
                            .append(INJECT_PERMISSION_NOTIFY_CONTENT)
                            .append("super.onRequestPermissionsResult(requestCode, permissions, grantResults);\n")
                            .append("}")
                    project.logger.error "SimplePermission-----> add notifyPermission method :\n " + methodBuilder.toString()

                    notifyMethod = CtNewMethod.make(methodBuilder.toString(), c)
                    c.addMethod(notifyMethod)
                }
                //在加了PermissionRequest注解的方法内插入权限请求的代码，根据PermissionRequest.needReCall来决定是否需要在权限回调成功的方法里插入代码
                for (CtMethod method : c.getDeclaredMethods()) {
                    PermissionRequest permissionRequest = method.getAnnotation(PermissionRequest.class)
                    //没有注解的方法忽略
                    if (null == permissionRequest) {
                        continue
                    }
                    project.logger.error "SimplePermission-----> method returnType : " + method.getReturnType().name
                    //带返回值的方法暂时忽略
                    if (!method.getReturnType().name.contains("void")) {
                        continue
                    }
                    //静态方法忽略
                    if (Modifier.isStatic(method.getModifiers())) {
                        continue
                    }
                    project.logger.error "SimplePermission-----> find requestPermission method : " + method.longName
                    //因为javassist不支持"{"与"}"有多行数据生成数组，所以要用一个String[] xx=new String[n]形式的数组把权限存储下来
                    StringBuilder requestMethodBuilder = new StringBuilder()
                            .append("String []requestPermissions = new String[")
                            .append(permissionRequest.requestPermissions().length)
                            .append("];\n")
                    int eachIndex = 0
                    for (String permission : permissionRequest.requestPermissions()) {
                        requestMethodBuilder.append("requestPermissions[")
                                .append(eachIndex)
                                .append("] = \"")
                                .append(permission)
                                .append("\";\n")
                        eachIndex++
                    }

                    //因为前面已经排除了非Activity和Fragment的class
                    boolean isActivity = c.subclassOf(pool.get(ACTIVITY_PATH))
                    requestMethodBuilder.append("final boolean hasPermissions = PermissionsManager.getInstance().hasAllPermissions(")
                    // $0代码的是this，$1代表方法参数的第一个参数、$2代表方法参数的第二个参数,以此类推，$N代表是方法参数的第N个。
                            .append("\$0")
                    if (!isActivity) {
                        requestMethodBuilder.append(".getActivity()")
                    }
                    requestMethodBuilder.append(",")
                            .append("requestPermissions );\n")
                            .append("if (!hasPermissions) {\n")
                    //如果需要在权限申请成功后继续执行此方法的逻辑代码，则需要存储参数
                    if (permissionRequest.needReCall()) {
                        //开始存储方法参数
                        CtClass[] mParameterTypes = method.getParameterTypes()
                        //权限申请成功的方法插入代码
                        CtMethod onSuccessMethod = c.getDeclaredMethod("onSuccess", CtClass.intType)
                        if (null != onSuccessMethod) {
                            StringBuilder onSuccessMethodBuilder = new StringBuilder("List params = requestPermissionMethodParams.get(Integer.valueOf(\$1));\n")
                                    .append("if(\$1==")
                                    .append(permissionRequest.requestCode())
                                    .append("){\n")
                                    .append(method.getName())
                                    .append("(")
                            if (null != mParameterTypes && mParameterTypes.length > 0) {
                                for (int k = 0; k < mParameterTypes.length; k++) {
                                    onSuccessMethodBuilder.append("(")
                                            .append(mParameterTypes[0].name)
                                            .append(")")
                                            .append("params.get(")
                                            .append(k)
                                            .append(")")
                                    if (k != mParameterTypes.length - 1) {
                                        onSuccessMethodBuilder.append(",")
                                    }
                                }
                            }
                            onSuccessMethodBuilder.append()
                                    .append(");\n")
                                    .append("return;\n")
                                    .append("}\n")

                            project.logger.error "SimplePermission-----> onSuccess method insert content :\n " + onSuccessMethodBuilder.toString()
                            onSuccessMethod.insertBefore(onSuccessMethodBuilder.toString())
                        }
                        //在申请权限的方法内部插入存储参数的代码
                        if (null != mParameterTypes && mParameterTypes.length > 0) {
                            requestMethodBuilder.append("List params = new java.util.ArrayList();\n")
                            for (int k = 0; k < mParameterTypes.length; k++) {
                                requestMethodBuilder.append("params.add(\$")
                                        .append(k + 1)
                                        .append(");\n")
                            }
                            requestMethodBuilder.append("requestPermissionMethodParams.put(")
                                    .append("Integer.valueOf(")
                                    .append(permissionRequest.requestCode())
                                    .append(")")
                                    .append(",")
                                    .append("params);\n")
                        }
                    }
                    requestMethodBuilder.append("PermissionsManager.getInstance().requestPermissionsIfNecessaryForResult(")
                            .append(permissionRequest.requestCode())//requestCode
                            .append(",")
                            .append("\$0,")
                            .append("requestPermissions,")
                            .append("\$0);\n")
                    //申请权限的方法的代码逻辑需要等到权限申请成功后才能执行的话，这里要在申请权限的代码后加上return，所以这个库目前有一个限制：带返回参数的方法暂不支持
                    if (permissionRequest.needReCall()) {
                        requestMethodBuilder.append("return;\n")
                    }
                    requestMethodBuilder.append("}\n")
                    project.logger.error "SimplePermission-----> requestPermission method insert content :\n " + requestMethodBuilder.toString()
                    method.insertBefore(requestMethodBuilder.toString())
                }

                c.writeFile(path)
                c.detach()
            } catch (Exception e) {
                c.detach()
                e.printStackTrace()
                throw new RuntimeException(e)
            }
        }
    }

    static CtMethod findMethodByName(CtClass ctClass, String methodName) {
        //getDeclaredMethods获取自己申明的方法，c.getMethods()会把所有父类的方法都加上
        for (CtMethod method : ctClass.getDeclaredMethods()) {
            if (method.getLongName().contains(methodName)) {
                return method
            }
        }
        return null
    }

    static CtField findFieldByName(CtClass ctClass, String fieldName) {
        for (CtField field : ctClass.getDeclaredFields()) {
            if (field.getName().equals(fieldName)) {
                return field
            }
        }
        return null
    }
}