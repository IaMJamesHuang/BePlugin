package com.kt.james.beplugincore.android;

import android.content.Context;

import com.kt.james.beplugincore.util.ReflectInvoker;

/**
 * author: James
 * 2019/4/14 13:20
 * version: 1.0
 */
public class HackContextWrapper {

    private static final String ClassName = "android.content.ContextWrapper";

    private static final String Field_mBase = "mBase";

    protected Object instance;

    public HackContextWrapper(Object obj) {
        instance = obj;
    }

    public Context getBase() {
        return (Context) ReflectInvoker.getField(instance, ClassName, Field_mBase);
    }

    public void setBase(Object obj) {
        ReflectInvoker.setField(instance, ClassName, Field_mBase, obj);
    }

}
