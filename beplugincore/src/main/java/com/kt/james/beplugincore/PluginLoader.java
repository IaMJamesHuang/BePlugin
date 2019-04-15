package com.kt.james.beplugincore;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;

import com.kt.james.beplugincore.android.CompatForSupportv7ViewInflater;
import com.kt.james.beplugincore.android.HackLayoutInflater;
import com.kt.james.beplugincore.content.LoadedPlugin;
import com.kt.james.beplugincore.content.PluginInfo;
import com.kt.james.beplugincore.manager.PluginManagerProviderClient;
import com.kt.james.beplugincore.processor.ActivityStubMappingProcessor;
import com.kt.james.beplugincore.util.LogUtil;
import com.kt.james.beplugincore.util.ProcessUtil;

/**
 * author: James
 * 2019/4/11 0:05
 * version: 1.0
 */
public class PluginLoader {

    public static synchronized void initLoader(Application application) {
        if (BePluginGlobal.isInit()) {
            return;
        }

        LogUtil.d("框架初始化开始");
        long start = System.currentTimeMillis();

        BePluginGlobal.setHostApplication(application);

        boolean isPluginProcess = ProcessUtil.isPluginProcess();

        BePluginGlobal.registerMappingProcessor(new ActivityStubMappingProcessor());

        PluginInjector.injectInstrumentation();
        PluginInjector.injectBaseContext(BePluginGlobal.getHostApplication());

        if (isPluginProcess) {
            HackLayoutInflater.installPluginCustomViewConstructorCache();
            CompatForSupportv7ViewInflater.installPluginCustomViewConstructorCache();
            BePluginGlobal.getHostApplication().registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
                @Override
                public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

                }

                @Override
                public void onActivityStarted(Activity activity) {

                }

                @Override
                public void onActivityResumed(Activity activity) {

                }

                @Override
                public void onActivityPaused(Activity activity) {

                }

                @Override
                public void onActivityStopped(Activity activity) {

                }

                @Override
                public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

                }

                @Override
                public void onActivityDestroyed(Activity activity) {
                    //回收绑定关系
                    Intent intent = activity.getIntent();
                    if (intent != null && intent.getComponent() != null) {
                        PluginManagerProviderClient.unBindStubActivity(intent.getComponent().getClassName(), activity.getClass().getName());
                    }
                }
            });
        }

        BePluginGlobal.setIsInit(true);

        long end = System.currentTimeMillis();
        LogUtil.d("框架初始化结束，耗时：" + (end - start));
    }

    public static Class loadPluginClassByName(String clazzName) {
        PluginInfo pluginInfo = PluginManagerProviderClient.queryPluginInfoByClazzName(clazzName);
        return loadPluginClassByName(pluginInfo, clazzName);
    }

    public static Class loadPluginClassByName(PluginInfo pluginInfo, String clazzName) {
        LoadedPlugin loadedPlugin = PluginLauncher.getInstance().wakeupPlugin(pluginInfo);
        if (pluginInfo == null || clazzName == null) {
            LogUtil.e("Plugin为空？", pluginInfo == null, "clazzName为空？", clazzName == null);
            return null;
        }
        if (loadedPlugin != null) {
            return loadedPlugin.loadClassByName(clazzName);
        } else {
            LogUtil.e("插件未启动");
        }
        return null;
    }

}
