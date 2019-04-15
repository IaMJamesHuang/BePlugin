package com.kt.james.beplugincore.android;

import android.app.Application;
import android.app.Instrumentation;
import android.content.pm.ActivityInfo;

import com.kt.james.beplugincore.util.ReflectInvoker;

/**
 * author: James
 * 2019/4/15 0:32
 * version: 1.0
 */
public class HackActivity extends HackContextThemeWrapper{

    private static final String ClassName = "android.app.Activity";

    private static final String Field_mActivityInfo = "mActivityInfo";
    private static final String Field_mApplication = "mApplication";
    private static final String Field_mInstrumentation = "mInstrumentation";

    public HackActivity(Object instance) {
        super(instance);
    }

    public ActivityInfo getActivityInfo() {
        return (ActivityInfo) ReflectInvoker.getField(instance, ClassName, Field_mActivityInfo);
    }

    public final void setApplication(Application application) {
        ReflectInvoker.setField(instance, ClassName, Field_mApplication, application);
    }

    public final void setInstrumentation(Instrumentation instrumentation) {
        ReflectInvoker.setField(instance, ClassName, Field_mInstrumentation, instrumentation);
    }

    public final Instrumentation getInstrumentation() {
        return (Instrumentation) ReflectInvoker.getField(instance, ClassName, Field_mInstrumentation);
    }

}
