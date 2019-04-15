package com.kt.james.beplugincore;

import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;

import com.kt.james.beplugincore.content.LoadedPlugin;
import com.kt.james.beplugincore.content.PluginInfo;
import com.kt.james.beplugincore.util.LogUtil;
import com.kt.james.beplugincore.util.ProcessUtil;

import java.util.concurrent.ConcurrentHashMap;

/**
 * author: James
 * 2019/4/12 16:19
 * version: 1.0
 */
public class PluginLauncher {

    private static PluginLauncher instance;

    private ConcurrentHashMap<String, LoadedPlugin> loadedPluginMap = new ConcurrentHashMap<>();

    private PluginLauncher() {
        if (!ProcessUtil.isPluginProcess()) {
            throw new IllegalStateException("本类只能在插件进程中使用");
        }
    }

    public static PluginLauncher getInstance() {
        if (instance == null) {
            synchronized (PluginLauncher.class) {
                if (instance == null) {
                    instance = new PluginLauncher();
                }
            }
        }
        return instance;
    }

    public synchronized LoadedPlugin getRunningPlugin(String packageName) {
        return loadedPluginMap.get(packageName);
    }

    public synchronized boolean isRunning(String packageName) {
        return loadedPluginMap.containsKey(packageName);
    }

    public synchronized LoadedPlugin wakeupPlugin(final PluginInfo pluginInfo) {
        LoadedPlugin plugin = loadedPluginMap.get(pluginInfo.getPackageName());
        if (plugin == null) {
            long start = System.currentTimeMillis();
            LogUtil.d("开始唤醒插件\n 插件信息：" , pluginInfo.getPackageName(), pluginInfo.getInstallPath(), pluginInfo.getVersionName());

            //创建Resources
            Resources resources = PluginCreator.createPluginResources(
                    BePluginGlobal.getHostApplication().getApplicationInfo().sourceDir,
                    BePluginGlobal.getHostApplication().getResources(),
                    pluginInfo);

            //创建classLoader
            ClassLoader classLoader = PluginCreator.createPluginClassLoader(pluginInfo.getInstallPath(), pluginInfo.isStandalone());

            //创建context
            Context context = PluginCreator.createPluginContext(pluginInfo,
                    BePluginGlobal.getHostApplication().getBaseContext(),
                    resources,
                    classLoader);

            //设置默认主题
            context.setTheme(pluginInfo.getApplicationTheme());

            //创建loadPlugin
            plugin = new LoadedPlugin(classLoader,
                    resources,
                    context,
                    pluginInfo.getPackageName(),
                    pluginInfo.getInstallPath());

            loadedPluginMap.put(pluginInfo.getPackageName(), plugin);

            //初始化插件的application
            if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
                initApplication(plugin, pluginInfo);
            } else {
                final LoadedPlugin finalPlugin = plugin;
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        initApplication(finalPlugin, pluginInfo);
                    }
                });
            }
            long end = System.currentTimeMillis();
            LogUtil.d("唤醒插件完毕，耗时：" + (end - start));
        }
        return plugin;
    }

    private void initApplication(LoadedPlugin plugin, PluginInfo pluginInfo) {
        Application pluginApplication = callApplicationOnCreate(plugin, pluginInfo);
        plugin.pluginApplication = pluginApplication;
    }

    private Application callApplicationOnCreate(LoadedPlugin plugin, PluginInfo pluginInfo) {
        Application application = null;
        ClassLoader classLoader = plugin.classLoader;
        Context context = plugin.context;

        try {
            application = Instrumentation.newApplication(classLoader.loadClass(pluginInfo.getApplicationName()),context);
        } catch (Exception e) {
            LogUtil.printException("PluginLauncher.callApplicationOnCreate", e);
            throw new RuntimeException("反射application的时候出错了。。。");
        }
        //安装ContentProvider, 这里先省略

        ((PluginContextTheme)context).setPluginApplication(application);

        //call onCreate
        application.onCreate();

        return application;
    }


}
