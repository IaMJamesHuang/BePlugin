package com.kt.james.beplugincore.android;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;

import com.kt.james.beplugincore.util.ReflectInvoker;

/**
 * author: James
 * 2019/4/15 10:11
 * version: 1.0
 */
public class HackContextThemeWrapper extends HackContextWrapper {

    private static final String ClassName = "android.view.ContextThemeWrapper";

    private static final String Field_mResources = "mResources";
    private static final String Field_mTheme = "mTheme";

    private static final String Method_attachBaseContext = "attachBaseContext";

    public HackContextThemeWrapper(Object obj) {
        super(obj);
    }

    public final void attachBaseContext(Object paramValues) {
        ReflectInvoker.invokeMethod(instance, ClassName, Method_attachBaseContext, new Class[]{Context.class}, new Object[]{paramValues});
    }

    public final void setResources(Resources resources) {
        if (Build.VERSION.SDK_INT > 16) {
            ReflectInvoker.setField(instance, ClassName, Field_mResources, resources);
        }
    }

    public final void setTheme(Resources.Theme theme) {
        ReflectInvoker.setField(instance, ClassName, Field_mTheme, theme);
    }

}
