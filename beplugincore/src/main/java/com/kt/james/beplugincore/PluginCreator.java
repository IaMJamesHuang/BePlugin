package com.kt.james.beplugincore;

import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;

import com.kt.james.beplugincore.android.HackAssetManager;
import com.kt.james.beplugincore.content.PluginInfo;
import com.kt.james.beplugincore.util.LogUtil;
import com.kt.james.beplugincore.wrapper.PluginResourceWrapper;

import java.io.File;

/**
 * author: James
 * 2019/4/12 15:20
 * version: 1.0
 */
public class PluginCreator {

    public static Context createPluginContext(PluginInfo pluginInfo, Context base, Resources resources, ClassLoader classLoader) {
        return new PluginContextTheme(base, pluginInfo, resources, classLoader);
    }

    public static Resources createPluginResources(String mainApkPath, Resources mainRes, PluginInfo pluginInfo) {
        String apkPath = pluginInfo.getInstallPath();
        if (new File(apkPath).exists()) {
            try {
                boolean isStandalone = pluginInfo.isStandalone();
                String[] assetPaths = buildAssetPath(isStandalone, mainApkPath, apkPath);
                AssetManager assetMgr = AssetManager.class.newInstance();
                HackAssetManager hackAssetManager = new HackAssetManager(assetMgr);
                for(String path : assetPaths) {
                    hackAssetManager.addAssetPath(path);
                }
                return new PluginResourceWrapper(assetMgr, mainRes.getDisplayMetrics(), mainRes.getConfiguration(), pluginInfo);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        } else {
            //插件apk已经损坏，需要卸载
        }
        return null;
    }

    private static String[] buildAssetPath(boolean isStandalone, String mainApkPath, String pluginApkPath) {
        String[] assetPaths = new String[isStandalone ? 1 : 2];
        assetPaths[0] = pluginApkPath;
        if (!isStandalone) {
            assetPaths[1] = mainApkPath;
        }
        return assetPaths;
    }

    public static ClassLoader createPluginClassLoader(String absolutePluginApkPath, boolean isStandalone) {
        String apkParentDir = new File(absolutePluginApkPath).getParent();

        File optDir = new File(apkParentDir, "dalvik-cache");
        optDir.mkdir();

        File libDir = new File(apkParentDir, "lib");
        libDir.mkdir();

        LogUtil.d(absolutePluginApkPath, optDir.getAbsolutePath(), libDir.getAbsolutePath());

        if (!isStandalone) {
            return new PluginClassLoader("", new RealPluginClassLoader(
                    absolutePluginApkPath,
                    optDir.getAbsolutePath(),
                    libDir.getAbsolutePath(),
                    PluginLoader.class.getClassLoader()
            ));
        } else {
            return new PluginClassLoader("", new RealPluginClassLoader(
                    absolutePluginApkPath,
                    optDir.getAbsolutePath(),
                    libDir.getAbsolutePath(),
                    ClassLoader.getSystemClassLoader()
            ));
        }
    }

    public static Context createPluginComponentContext(Context pluginContext, Context base) {
        PluginContextTheme pluginContextTheme = null;
        if (pluginContext != null) {
            pluginContextTheme = (PluginContextTheme) PluginCreator.createPluginContext(
                    ((PluginContextTheme)pluginContext).getPluginInfo(),
                    base,
                    pluginContext.getResources(),
                    pluginContext.getClassLoader());
            pluginContextTheme.setPluginApplication((Application) ((PluginContextTheme) pluginContext).getApplicationContext());
            pluginContextTheme.setTheme(BePluginGlobal.getHostApplication().getApplicationContext().getApplicationInfo().theme);
        }
        return pluginContextTheme;
    }

}
