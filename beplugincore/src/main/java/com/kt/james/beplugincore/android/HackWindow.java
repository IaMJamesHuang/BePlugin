package com.kt.james.beplugincore.android;

import android.content.Context;
import android.view.LayoutInflater;

import com.kt.james.beplugincore.util.ReflectInvoker;

/**
 * author: James
 * 2019/4/15 10:20
 * version: 1.0
 */
public class HackWindow {

    private static final String ClassName = "android.view.Window";

    private static final String Field_mContext = "mContext";
    private static final String Field_mWindowStyle = "mWindowStyle";
    private static final String Field_mLayoutInflater = "mLayoutInflater";

    private Object instance;

    public HackWindow(Object instance) {
        this.instance = instance;
    }

    public void setContext(Context context) {
        ReflectInvoker.setField(instance, ClassName, Field_mContext, context);
    }

    public void setWindowStyle(Object style) {
        ReflectInvoker.setField(instance, ClassName, Field_mWindowStyle, style);
    }

    public void setLayoutInflater(String className, LayoutInflater layoutInflater) {
        ReflectInvoker.setField(instance, className, Field_mLayoutInflater, layoutInflater);
    }

}
