package com.ykbjson.lib.plugin

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

public class SimplePermissionTransform extends Transform {


    private Project mProject;

    SimplePermissionTransform(Project mProject) {
        this.mProject = mProject
    }

    // 设置我们自定义的Transform对应的Task名称
    // 类似：TransformClassesWithPreDexForXXX
    @Override
    String getName() {
        return "SimplePermissionTransform"
    }

    // 指定输入的类型，通过这里的设定，可以指定我们要处理的文件类型
    //这样确保其他类型的文件不会传入
    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    //    指Transform要操作内容的范围，官方文档Scope有7种类型：
    //
    //    EXTERNAL_LIBRARIES        只有外部库
    //    PROJECT                       只有项目内容
    //    PROJECT_LOCAL_DEPS            只有项目的本地依赖(本地jar)
    //    PROVIDED_ONLY                 只提供本地或远程依赖项
    //    SUB_PROJECTS              只有子项目。
    //    SUB_PROJECTS_LOCAL_DEPS   只有子项目的本地依赖项(本地jar)。
    //    TESTED_CODE                   由当前变量(包括依赖项)测试的代码
    @Override
    Set<QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    /**
     * 具体的处理
     */
    @Override
    void transform(Context context, Collection<TransformInput> inputs,
                   Collection<TransformInput> referencedInputs,
                   TransformOutputProvider outputProvider, boolean isIncremental)
            throws IOException, TransformException, InterruptedException {
        mProject.logger.error "==============SimplePermission transform start=============="
        try {
            // Transform的inputs有两种类型，一种是目录，一种是jar包，要分开遍历
            inputs.each { TransformInput input ->
                //对类型为“文件夹”的input进行遍历
                input.directoryInputs.each { DirectoryInput directoryInput ->
                    //文件夹里面包含的是我们手写的类以及R.class、BuildConfig.class以及R$XXX.class等
//               String packageName=project.extensions.findByName("applicationId")
                    SimplePermissionPluginInject.injectDir(mProject, directoryInput.file.absolutePath, "com/ykbjson/app/simplepermission")
                    // 获取output目录
                    def dest = outputProvider.getContentLocation(directoryInput.name,
                            directoryInput.contentTypes, directoryInput.scopes,
                            Format.DIRECTORY)

                    // 将input的目录复制到output指定目录
                    FileUtils.copyDirectory(directoryInput.file, dest)
                }
                //对类型为jar文件的input进行遍历
                input.jarInputs.each { JarInput jarInput ->
                    //jar文件一般是第三方依赖库jar文件
                    SimplePermissionPluginInject.injectJar(mProject, jarInput.file.absolutePath, "com/ykbjson/app/simplepermission")
                    // 重命名输出文件（同目录copyFile会冲突）
                    def jarName = jarInput.name
                    def md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
                    if (jarName.endsWith(".jar")) {
                        jarName = jarName.substring(0, jarName.length() - 4)
                    }
                    //生成输出路径
                    def dest = outputProvider.getContentLocation(jarName + md5Name,
                            jarInput.contentTypes, jarInput.scopes, Format.JAR)
                    //将输入内容复制到输出
                    FileUtils.copyFile(jarInput.file, dest)
                }
            }

        } catch (Exception e) {
            SimplePermissionPluginInject.removeClassPath(mProject)
            throw new RuntimeException(e)
        }
        SimplePermissionPluginInject.removeClassPath(mProject)
        mProject.logger.error "==============SimplePermission transform end=============="
    }
}