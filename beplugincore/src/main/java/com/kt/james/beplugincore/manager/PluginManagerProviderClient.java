package com.kt.james.beplugincore.manager;

import android.content.ContentResolver;
import android.os.Bundle;
import android.text.TextUtils;

import com.kt.james.beplugincore.BePluginGlobal;
import com.kt.james.beplugincore.content.InstallResult;
import com.kt.james.beplugincore.content.PluginInfo;
import com.kt.james.beplugincore.constant.ResultCodeConstant;

import java.util.ArrayList;

/**
 * author: James
 * 2019/4/10 23:51
 * version: 1.0
 */
public class PluginManagerProviderClient {

    public static ArrayList<PluginInfo> queryAllInstallPlugins() {
        ContentResolver resolver = BePluginGlobal.getHostApplication().getContentResolver();
        Bundle bundle = resolver.call(PluginManagerProvider.buildCallUri(), PluginManagerProvider.ACTION_QUERY_ALL, null, null);
        if (bundle != null) {
            return (ArrayList<PluginInfo>) bundle.getSerializable(PluginManagerProvider.RESULT_QUERY_ALL);
        }
        return null;
    }

    public static PluginInfo queryPluginInfoByClazzName(String clazzName) {
        if (TextUtils.isEmpty(clazzName)) {
            return null;
        }
        ContentResolver resolver = BePluginGlobal.getHostApplication().getContentResolver();
        Bundle bundle = resolver.call(PluginManagerProvider.buildCallUri(), PluginManagerProvider.ACTION_QUERY_BY_CLASS, clazzName, null);
        if (bundle != null) {
            return (PluginInfo) bundle.getSerializable(PluginManagerProvider.RESULT_QUERY_BY_CLASS);
        }
        return null;
    }

    public static PluginInfo queryPluginInfoByPackageName(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return null;
        }
        ContentResolver resolver = BePluginGlobal.getHostApplication().getContentResolver();
        Bundle bundle = resolver.call(PluginManagerProvider.buildCallUri(), PluginManagerProvider.ACTION_QUERY,
                packageName, null);
        if (bundle != null) {
            return (PluginInfo) bundle.get(PluginManagerProvider.RESULT_QUERY);
        }
        return null;
    }

    public static InstallResult installPlugin(String apkPath) {
        if (TextUtils.isEmpty(apkPath)) {
            return new InstallResult(ResultCodeConstant.SRC_FILE_NOT_FOUND);
        }
        ContentResolver resolver = BePluginGlobal.getHostApplication().getContentResolver();
        Bundle bundle = resolver.call(PluginManagerProvider.buildCallUri(),
                PluginManagerProvider.ACTION_INSTALL, apkPath, null);
        if (bundle != null) {
            return (InstallResult) bundle.get(PluginManagerProvider.RESULT_INSTALL);
        }
        return new InstallResult(ResultCodeConstant.IPC_ERROR);
    }

    public static int wakeupPlugin(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return ResultCodeConstant.WAKEUP_FAIL;
        }
        ContentResolver resolver = BePluginGlobal.getHostApplication().getContentResolver();
        Bundle bundle = resolver.call(PluginManagerProvider.buildCallUri(), PluginManagerProvider.ACTION_WAKEUP, packageName, null);
        if (bundle != null) {
            return bundle.getInt(PluginManagerProvider.RESULT_WAKEUP, ResultCodeConstant.WAKEUP_FAIL);
        }
        return ResultCodeConstant.WAKEUP_FAIL;
    }

    public static void wakeupAllPlugins() {
        ContentResolver resolver = BePluginGlobal.getHostApplication().getContentResolver();
        resolver.call(PluginManagerProvider.buildCallUri(), PluginManagerProvider.ACTION_WAKEUP_ALL, null, null);
    }

    public static int removePlugin(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return ResultCodeConstant.REMOVE_FAIL;
        }
        ContentResolver resolver = BePluginGlobal.getHostApplication().getContentResolver();
        Bundle bundle = resolver.call(PluginManagerProvider.buildCallUri(), PluginManagerProvider.ACTION_REMOVE, packageName, null);
        if (bundle != null) {
            return bundle.getInt(PluginManagerProvider.RESULT_REMOVE, ResultCodeConstant.REMOVE_FAIL);
        }
        return ResultCodeConstant.REMOVE_FAIL;
    }

    public static String bindStubActivity(String pluginActivityName, String packageName) {
        Bundle bundle = new Bundle();
        bundle.putString("packageName", packageName);
        bundle.putString("pluginActivityName", pluginActivityName);

        ContentResolver resolver = BePluginGlobal.getHostApplication().getContentResolver();
        Bundle result = resolver.call(PluginManagerProvider.buildCallUri(), PluginManagerProvider.ACTION_BIND_STUB_ACTIVITY, null, bundle);
        if (result != null) {
            return result.getString(PluginManagerProvider.RESULT_BIND_STUB_ACTIVITY);
        }
        return null;
    }

    public static void unBindStubActivity(String stubActivityName, String pluginActivityName) {
        Bundle bundle = new Bundle();
        bundle.putString("pluginActivity", pluginActivityName);
        ContentResolver resolver = BePluginGlobal.getHostApplication().getContentResolver();
        resolver.call(PluginManagerProvider.buildCallUri(), PluginManagerProvider.ACTION_UNBIND_STUB_ACTIVITY, stubActivityName, bundle);
    }

    public static boolean isStub(String className) {
        //如果指定Stub前缀为特定值，可以减少跨进程调用
        ContentResolver resolver = BePluginGlobal.getHostApplication().getContentResolver();
        Bundle result = resolver.call(PluginManagerProvider.buildCallUri(), PluginManagerProvider.ACTION_IS_STUB, className, null);
        if (result != null) {
            return result.getBoolean(PluginManagerProvider.RESULT_IS_STUB, false);
        }
        return false;
    }

}
