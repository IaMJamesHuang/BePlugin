package com.kt.james.beplugincore;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.view.LayoutInflater;

import com.kt.james.beplugincore.android.HackResources;
import com.kt.james.beplugincore.content.PluginInfo;
import com.kt.james.beplugincore.util.LogUtil;
import com.kt.james.beplugincore.util.ProcessUtil;
import com.kt.james.beplugincore.wrapper.PluginBaseContextWrapper;

/**
 * author: James
 * 2019/4/12 19:01
 * version: 1.0
 */
public class PluginContextTheme extends PluginBaseContextWrapper {

    private int mThemeResource;
    Resources.Theme mTheme;
    private LayoutInflater mLayoutInflater;
    private ApplicationInfo mApplicationInfo;
    private Resources mResources;
    private ClassLoader mClassloader;
    private Application mPluginApplication;
    private PluginInfo mPluginInfo;

    public PluginContextTheme(Context base, PluginInfo pluginInfo, Resources resources, ClassLoader classLoader) {
        super(base);
        mPluginInfo = pluginInfo;
        mResources = resources;
        mClassloader = classLoader;
        if (!ProcessUtil.isPluginProcess()) {
            throw new IllegalStateException("本类只能在插件进程中使用");
        }
    }

    public void setPluginApplication(Application application) {
        mPluginApplication = application;
    }

    @Override
    public ClassLoader getClassLoader() {
        return mClassloader;
    }

    @Override
    public AssetManager getAssets() {
        return mResources.getAssets();
    }

    @Override
    public Resources getResources() {
        return mResources;
    }

    /**
     * 传0表示使用系统默认主题，最终的现实样式和客户端程序的minSdk应该有关系。
     * 即系统针对不同的minSdk设置了不同的默认主题样式
     * 传非0的话表示传过来什么主题就显示什么主题
     */
    @Override
    public void setTheme(int resid) {
        mThemeResource = resid;
        initializeTheme();
    }

    private void initializeTheme() {
        final boolean first = mTheme == null;
        if (first) {
            mTheme = getResources().newTheme();
            Resources.Theme theme = getBaseContext().getTheme();
            if (theme != null) {
                mTheme.setTo(theme);
            }
        }
        mTheme.applyStyle(mThemeResource, true);
    }

    @Override
    public Resources.Theme getTheme() {
        if (mTheme != null) {
            return mTheme;
        }
        Integer result = HackResources.selectDefaultTheme(mThemeResource, getBaseContext().getApplicationInfo().targetSdkVersion);
        if (result != null) {
            mThemeResource = result;
        }
        initializeTheme();
        return mTheme;
    }

    @Override
    public Object getSystemService(String name) {
        if (LAYOUT_INFLATER_SERVICE.equals(name)) {
            if (mLayoutInflater == null) {
                mLayoutInflater = LayoutInflater.from(getBaseContext()).cloneInContext(this);
            }
            return mLayoutInflater;
        }
        return getBaseContext().getSystemService(name);
    }

    @Override
    public String getPackageName() {
        //packagemanager、activitymanager、wifi、window、inputservice
        //等等系统服务会获取packageName去查询信息，如果获取到插件的packageName则会crash
        return BePluginGlobal.getHostApplication().getPackageName();
    }

    @Override
    public Context getApplicationContext() {
        return mPluginApplication;
    }

    @Override
    public ApplicationInfo getApplicationInfo() {
        //这里的ApplicationInfo是从LoadedApk中取出来的
        //由于目前插件之间是共用1个插件进程。LoadedApk只有1个，而ApplicationInfo每个插件都有一个，
        // 所以不能通过直接修改loadedApk中的内容来修正这个方法的返回值，而是将修正的过程放在Context中去做，
        //避免多个插件之间造成干扰
        if (mApplicationInfo == null) {
            try {
                mApplicationInfo = getPackageManager().getApplicationInfo(BePluginGlobal.getHostApplication().getPackageName(), 0);
                mApplicationInfo.packageName = mPluginInfo.getPackageName();
            } catch (PackageManager.NameNotFoundException e) {
                LogUtil.printException("PluginContextTheme.getApplicationInfo", e);
            }
        }
        return mApplicationInfo;
    }

    @Override
    public String getPackageCodePath() {
        return mPluginInfo.getInstallPath();
    }

    public PluginInfo getPluginInfo() {
        return mPluginInfo;
    }

}
