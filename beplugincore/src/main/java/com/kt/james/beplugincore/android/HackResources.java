package com.kt.james.beplugincore.android;

import com.kt.james.beplugincore.util.ReflectInvoker;

/**
 * author: James
 * 2019/4/12 19:23
 * version: 1.0
 */
public class HackResources {

    private static final String ClassName = "android.content.res.Resources";

    private static final String Method_selectDefaultTheme = "selectDefaultTheme";

    public static Integer selectDefaultTheme(int mThemeResource,
                                             int targetSdkVersion) {
        return (Integer) ReflectInvoker.invokeMethod(null, ClassName, Method_selectDefaultTheme, new Class[]{
                int.class, int.class}, new Object[]{mThemeResource, targetSdkVersion});
    }

}
