package com.kt.james.beplugincore;

import dalvik.system.DexClassLoader;

/**
 * author: James
 * 2019/4/12 15:25
 * version: 1.0
 */
public class RealPluginClassLoader extends DexClassLoader {

    public RealPluginClassLoader(String dexPath, String optimizedDirectory, String librarySearchPath, ClassLoader parent) {
        super(dexPath, optimizedDirectory, librarySearchPath, parent);
    }

}
