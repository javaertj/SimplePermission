# 一、SimplePermission简介
基于注解调用的Android权限申请库,无需你手写申请权限的代码，也无需你在Activity或Fragment的onRequestPermissionsResult方法里写任何代码，只需在需要申请权限的类和方法上加上对应的注解即可，简单易用。

# 二、如何引用
因为这个功能是基于gradle插件来实现的，所以和一般权限库有所不同,需要在你的工程根目录的build.gradle文件相似位置里加上如下代码（看有注释的地方）。当前最新版本为
[ ![Download](https://api.bintray.com/packages/ykbjson/maven/simplepermissionplugin/images/download.svg) ](https://bintray.com/ykbjson/maven/simplepermissionplugin/_latestVersion)

'

	buildscript {
	   repositories {
	        google()
	        jcenter()
	        mavenCentral()
	        mavenLocal()
	        //这这我bintrary的maven地址
	        maven { url 'https://dl.bintray.com/ykbjson/maven' }
	    }
	    dependencies {
	        classpath 'com.android.tools.build:gradle:3.1.1'
	        //这是simplepermissionplugin的classpath
	        classpath 'com.ykbjson.simplepermission:simplepermissionplugin:1.0.0'
	  }
	}


	allprojects {
	    repositories {
	        google()
	        jcenter()
	        mavenCentral()
	        mavenLocal()
	        //这这我bintrary的maven地址
	        maven { url 'https://dl.bintray.com/ykbjson/maven' }
	    }
	}

'

然后在app module的build.gradle文件里引入apply simplepermissionplugin插件

'

	apply plugin: 'com.ykbjson.simplepermission'

'

最后在app module的build.gradle文件里引入simpleprmission库和simplepermission_ano库,即可大功告成

'

	implementation 'com.ykbjson.simplepermission:simplepermission:1.0.0'
    implementation 'com.ykbjson.simplepermission:simplepermission_ano:1.0.0'
	
'

# 三、如何使用

使用非常简单。
首先，在需要申请权限的Activity或Fragment类上加上注解@PermissionNotify

'

	@PermissionNotify
	public class MainActivity extends AppCompatActivity 
	//省略代码...
	}

'

然后在Activity或Fragment的某个方法上加上注解@PermissionRequest


'

	  @PermissionRequest(
            requestCode = 10010,
            requestPermissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.READ_CONTACTS}
                    ,needReCall = true
    )
    private void setText(String text) {
        mTextMessage.setText(text);
    }

'

然后。。。。。。结束啦！！！！

当然，如果你觉得使用注解会有诸多限制（请看下面第四条提到的“一些限制”）,你也可以直接使用simplepermission库来实现权限的申请，类似代码如下

'

	private void setText(final String text) {
        PermissionsManager.getInstance().requestPermissionsIfNecessaryForResult(0, this,
                new String[]{Manifest.permission.READ_CONTACTS}, new PermissionsRequestCallback() {
                    @Override
                    public void onGranted(int requestCode, String permission) {
                        
                    }

                    @Override
                    public void onDenied(int requestCode, String permission) {

                    }

                    @Override
                    public void onDeniedForever(int requestCode, String permission) {

                    }

                    @Override
                    public void onFailure(int requestCode, String[] deniedPermissions) {

                    }

                    @Override
                    public void onSuccess(int requestCode) {
                        mTextMessage.setText(text);
                    }
                });
        mTextMessage.setText(text);
    }
	

'
对于一些特殊权限，可能需要在Activit或Fragment重载onRequestPermissionsResult方法，并且在该方法内部加入PermissionsManager.getInstance().notifyPermissionsChange(permissions,grantResults),类似如下代码

'

	@Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermissionsManager.getInstance().notifyPermissionsChange(permissions,grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


'

# 四、一些限制

##### 4.1 由于本人能力有限，本库目前还不支持在“带返回值”的方法上去加@PermissionRequest注解并且@PermissionRequest的needRecall设置为true，当然，needRecall设置为false时，是支持的。因为本库的原理是这样的：添加了@PermissionRequest注解的方法，我会先判断当前程序是否已经有了@PermissionRequest的requestPermissions里包含的权限，如果没有，则插入申请权限的代码，如果@PermissionRequest的needRecall为true，则需要存储方法的参数，以备权限回调成功的时候在调用此方法，然后插入“return”，结束方法执行；如果@PermissionRequest的needRecall为false，则这里只插入申请权限的代码，不在干预此方法的后续逻辑。

##### 4.2 本库目前还不支持在内部类里面的方法上加@PermissionRequest注解，因为permission权限申请库必要的一个参数是Activit或Fragment，如果是在内部类里面使用，我目前还无法得知如何获取该内部类持有的Activit或Fragment，尤其是在多层内部类嵌套的时候。

##### 4.3 本库因为修改了class文件插入了一些代码，很有可能会使应用程序出现multiDex异常，所以，在需要的时候，最好让你的程序支持multiDex


## License

'

	Copyright 2018 ykbjson
	
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	
	   http://www.apache.org/licenses/LICENSE-2.0
	
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.	


'

