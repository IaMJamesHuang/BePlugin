package com.kt.james.beplugincore;

import android.app.Application;

import com.kt.james.beplugincore.processor.StubMappingProcessor;

import java.util.ArrayList;
import java.util.List;

/**
 * author: James
 * 2019/4/10 21:27
 * version: 1.0
 */
public class BePluginGlobal {

    private static boolean mIsInit;

    private static Application mApp;

    private static List<StubMappingProcessor> mMappingProcessor = new ArrayList<>();

    public static Application getHostApplication() {
        if (mApp == null) {
            throw new IllegalStateException("BePlugin is not inited yet!");
        }
        return mApp;
    }

    public static void setHostApplication(Application application) {
        mApp = application;
    }

    public static boolean isInit() {
        return mIsInit;
    }

    public static void setIsInit(boolean isInit) {
        BePluginGlobal.mIsInit = isInit;
    }

    public static void registerMappingProcessor(StubMappingProcessor processor) {
        if (processor == null) {
            return;
        }
        if (!mMappingProcessor.contains(processor)) {
            mMappingProcessor.add(processor);
        }
    }

    public static List<StubMappingProcessor> getMappingProcessor() {
        return mMappingProcessor;
    }

}
