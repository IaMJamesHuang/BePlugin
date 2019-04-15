package com.kt.james.beplugincore;

import dalvik.system.PathClassLoader;

/**
 * author: James
 * 2019/4/12 15:31
 * version: 1.0
 */
public class PluginClassLoader extends PathClassLoader {

    public PluginClassLoader(String dexPath, ClassLoader parent) {
        super(dexPath, parent);
    }

}
