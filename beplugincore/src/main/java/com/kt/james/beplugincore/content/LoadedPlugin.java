package com.kt.james.beplugincore.content;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;

import com.kt.james.beplugincore.util.LogUtil;

/**
 * author: James
 * 2019/4/12 16:23
 * version: 1.0
 */
public class LoadedPlugin {

    public Application pluginApplication;

    public final ClassLoader classLoader;

    public final Resources resources;

    public final Context context;

    public final String pluginPackageName;

    public final String pluginSourceDir;

    public LoadedPlugin(ClassLoader classLoader, Resources resources, Context context, String pluginPackageName, String pluginSourceDir) {
        this.classLoader = classLoader;
        this.resources = resources;
        this.context = context;
        this.pluginPackageName = pluginPackageName;
        this.pluginSourceDir = pluginSourceDir;
    }

    public Class loadClassByName(String clazzName) {
        Class clazz = null;
        try {
            clazz = classLoader.loadClass(clazzName);
            LogUtil.d("加载插件类成功：" + clazzName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            LogUtil.d("加载插件类失败：" + clazzName);
        }
        return clazz;
    }

}
