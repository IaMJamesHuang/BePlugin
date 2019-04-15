package com.kt.james.beplugincore;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.Window;

import com.kt.james.beplugincore.android.HackActivity;
import com.kt.james.beplugincore.android.HackActivityThread;
import com.kt.james.beplugincore.android.HackContextThemeWrapper;
import com.kt.james.beplugincore.android.HackContextWrapper;
import com.kt.james.beplugincore.android.HackLayoutInflater;
import com.kt.james.beplugincore.android.HackWindow;
import com.kt.james.beplugincore.content.LoadedPlugin;
import com.kt.james.beplugincore.content.PluginActivityInfo;
import com.kt.james.beplugincore.content.PluginInfo;
import com.kt.james.beplugincore.manager.PluginManagerProviderClient;
import com.kt.james.beplugincore.util.ProcessUtil;
import com.kt.james.beplugincore.util.ResourceUtil;
import com.kt.james.beplugincore.wrapper.PluginBaseContextWrapper;

/**
 * author: James
 * 2019/4/11 0:15
 * version: 1.0
 */
public class PluginInjector {

    public static void injectInstrumentation() {
        HackActivityThread.wrapInstrumentation();
    }

    /**
     * 用于后续修改startService、SendBroadcast方法
     * @param context
     */
    public static void injectBaseContext(Context context) {
        HackContextWrapper wrapper = new HackContextWrapper(context);
        wrapper.setBase(new PluginBaseContextWrapper(wrapper.getBase()));
    }

    public static void injectActivityContext(Activity activity) {
        boolean isStubActivity = false;
        if (ProcessUtil.isPluginProcess()) {
            Intent intent = activity.getIntent();
            if (intent.getComponent() != null) {
                isStubActivity = PluginManagerProviderClient.isStub(intent.getComponent().getClassName());
            }
        }

        HackActivity hackActivity = new HackActivity(activity);
        if (isStubActivity) {
            Context pluginContext = null;
            PluginInfo pluginInfo = null;
            pluginInfo = PluginManagerProviderClient.queryPluginInfoByClazzName(activity.getClass().getName());
            if (pluginInfo == null) {
                throw new RuntimeException("找不到插件信息");
            }
            LoadedPlugin loadedPlugin = PluginLauncher.getInstance().getRunningPlugin(pluginInfo.getPackageName());
            if (loadedPlugin == null || loadedPlugin.pluginApplication == null) {
                throw new RuntimeException("插件没有初始化");
            }
            pluginContext = PluginCreator.createPluginComponentContext(loadedPlugin.context, activity.getBaseContext());
            Application pluginApp = loadedPlugin.pluginApplication;
            hackActivity.setApplication(pluginApp);

            PluginActivityInfo activityInfo = pluginInfo.getPluginActivities().get(activity.getClass().getName());
            ActivityInfo realActivity = hackActivity.getActivityInfo();
            int pluginAppTheme = getPluginTheme(realActivity, activityInfo, pluginInfo);

            resetActivityContext(pluginContext, activity, pluginAppTheme);

            activity.setTitle(activity.getClass().getName());
        } else {
            // 如果是打开宿主程序的activity，注入一个无害的Context，用来在宿主程序中startService和sendBroadcast时检查打开的对象是否是插件中的对象
            // 插入Context
            Context mainContext = new PluginBaseContextWrapper(activity.getBaseContext());
            hackActivity.setBase(null);
            hackActivity.attachBaseContext(mainContext);
        }
    }

    public static void resetActivityContext(Context pluginContext, Activity activity, int pluginTheme) {
        if (pluginContext == null) {
            return;
        }

        // 重设BaseContext
        HackContextThemeWrapper hackContextThemeWrapper = new HackContextThemeWrapper(activity);
        hackContextThemeWrapper.setBase(null);
        hackContextThemeWrapper.attachBaseContext(pluginContext);

        // 由于在attach的时候Resource已经被初始化了，所以需要重置Resource
        hackContextThemeWrapper.setResources(null);

        // 重设theme
        if (pluginTheme != 0) {
            hackContextThemeWrapper.setTheme(null);
            activity.setTheme(pluginTheme);
        }
        ((PluginContextTheme)pluginContext).mTheme = null;
        pluginContext.setTheme(pluginTheme);

        //处理window
        Window window = activity.getWindow();
        HackWindow hackWindow = new HackWindow(window);
        hackWindow.setContext(pluginContext);
        hackWindow.setWindowStyle(null);
        hackWindow.setLayoutInflater(window.getClass().getName(), LayoutInflater.from(activity));
//        if (Build.VERSION.SDK_INT >= 11) {
//            new HackLayoutInflater(window.getLayoutInflater()).setPrivateFactory(activity);
//        }
    }

    /**
     * 主题的选择顺序为 先选择插件Activity配置的主题，再选择插件Application配置的主题，
     * 如果是非独立插件，再选择宿主Activity主题
     * 如果是独立插件，再选择系统默认主题
     * @param activityInfo
     * @param pluginActivityInfo
     * @param pluginInfo
     * @return
     */
    private static int getPluginTheme(ActivityInfo activityInfo, PluginActivityInfo pluginActivityInfo, PluginInfo pluginInfo) {
        int pluginAppTheme = 0;
        if (pluginActivityInfo != null) {
            pluginAppTheme = ResourceUtil.parseResId(pluginActivityInfo.getTheme());
        }
        if (pluginAppTheme == 0) {
            pluginAppTheme = pluginInfo.getApplicationTheme();
        }
        if (pluginAppTheme == 0 && pluginInfo.isStandalone()) {
            pluginAppTheme = android.R.style.Theme_DeviceDefault;
        }
        if (pluginAppTheme == 0) {
            pluginAppTheme = activityInfo.getThemeResource();
        }
        return pluginAppTheme;
    }

}
