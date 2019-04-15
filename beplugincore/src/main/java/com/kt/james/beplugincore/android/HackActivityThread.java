package com.kt.james.beplugincore.android;

import android.app.Instrumentation;

import com.kt.james.beplugincore.wrapper.PluginInstrumentationWrapper;
import com.kt.james.beplugincore.util.LogUtil;
import com.kt.james.beplugincore.util.ReflectInvoker;

/**
 * author: James
 * 2019/4/12 21:10
 * version: 1.0
 */
public class HackActivityThread {

    private static final String ClassName = "android.app.ActivityThread";

    private static final String Method_currentActivityThread = "currentActivityThread";

    private static final String Field_mInstrumentation = "mInstrumentation";

    private static final String Field_mHiddenApiWarningShown = "mHiddenApiWarningShown";

    private static HackActivityThread hackActivityThread;

    private Object instance;

    private HackActivityThread(Object instance) {
        this.instance = instance;
    }

    //这个方法需要在主线程中调用，因为ActivityThread是存储在ThreadLocal中的，其他线程中取出来的肯定是null
    public synchronized static HackActivityThread get() {
        if (hackActivityThread == null) {
            Object instance = currentActivityThread();
            if (instance != null) {
                hackActivityThread = new HackActivityThread(instance);
            }
        }
        return hackActivityThread;
    }

    private static Object currentActivityThread() {
        return ReflectInvoker.invokeMethod(null, ClassName,
                Method_currentActivityThread, null, null);
    }

    public static void wrapInstrumentation() {
        HackActivityThread hackActivityThread = get();
        if (hackActivityThread != null) {
            Instrumentation originalInstrumentation = hackActivityThread.getInstrumentation();
            if (!(originalInstrumentation instanceof PluginInstrumentationWrapper)) {
                hackActivityThread.setInstrumentation(new PluginInstrumentationWrapper(originalInstrumentation));
            }
        } else {
            LogUtil.e("wrapInstrumentation", "hackActivityThread 为空");
        }
    }

    public Instrumentation getInstrumentation() {
        return (Instrumentation) ReflectInvoker.getField(instance, ClassName, Field_mInstrumentation);
    }

    public void setInstrumentation(Instrumentation instrumentation) {
        ReflectInvoker.setField(instance, ClassName, Field_mInstrumentation, instrumentation);
    }

    public void setHiddenApiWarningShow(boolean isShow) {
        ReflectInvoker.setField(instance, ClassName, Field_mHiddenApiWarningShown, isShow);
    }

}
