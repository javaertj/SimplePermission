package com.ykbjson.app.simplepermission;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

/**
 * Description：
 * Creator：yankebin
 * CreatedAt：2018/10/29
 */
public class TestApplication extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(base);
    }
}
