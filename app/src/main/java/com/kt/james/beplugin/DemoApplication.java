package com.kt.james.beplugin;

import android.app.Application;
import android.content.Context;

import com.kt.james.beplugincore.PluginLoader;

/**
 * author: James
 * 2019/4/14 13:45
 * version: 1.0
 */
public class DemoApplication extends Application {

    private static Application application;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        PluginLoader.initLoader(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
    }

    public static Application getApplication() {
        return application;
    }

}
