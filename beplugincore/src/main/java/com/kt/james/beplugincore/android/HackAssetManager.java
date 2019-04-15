package com.kt.james.beplugincore.android;

import com.kt.james.beplugincore.util.ReflectInvoker;

/**
 * author: James
 * 2019/4/11 16:55
 * version: 1.0
 */
public class HackAssetManager {

    private static final String ClassName = "android.content.res.AssetManager";

    private static final String Method_addAssetPath = "addAssetPath";

    private static final String Method_addAssetPaths = "addAssetPaths";

    private Object instance;

    public HackAssetManager(Object instance) {
        this.instance = instance;
    }

    public void addAssetPath(String path) {
        ReflectInvoker.invokeMethod(instance, ClassName, Method_addAssetPath, new Class[] {String.class}, new Object[] {path});
    }

    public void addAssetPaths(String[] path) {
        ReflectInvoker.invokeMethod(instance, ClassName, Method_addAssetPaths, new Class[] {String[].class}, new Object[] {path});
    }

}
