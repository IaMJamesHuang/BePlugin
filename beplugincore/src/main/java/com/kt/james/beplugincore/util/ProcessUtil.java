package com.kt.james.beplugincore.util;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;

import com.kt.james.beplugincore.BePluginGlobal;
import com.kt.james.beplugincore.manager.PluginManagerProvider;

import java.util.List;

/**
 * author: James
 * 2019/4/11 0:19
 * version: 1.0
 */
public class ProcessUtil {

    private static Boolean isPluginProcess;

    public static boolean isPluginProcess() {
        if (isPluginProcess == null) {
            String currentProcessName = getCurrentProcessName(BePluginGlobal.getHostApplication());
            String pluginProcessName = getPluginProcessName(BePluginGlobal.getHostApplication());
            isPluginProcess = currentProcessName.equals(pluginProcessName);
        }
        return isPluginProcess;
    }

    private static String getCurrentProcessName(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> list = activityManager.getRunningAppProcesses();
        if (list != null) {
            for (ActivityManager.RunningAppProcessInfo processInfo : list) {
                if (processInfo != null && processInfo.pid == android.os.Process.myPid()) {
                    return processInfo.processName;
                }
            }
        }
        return "";
    }

    private static String getPluginProcessName(Context context) {
        //provider运行在plugin进程中
        try {
            ProviderInfo providerInfo = context.getPackageManager().getProviderInfo(new ComponentName(context, PluginManagerProvider.class), 0);
            return providerInfo == null ? "" : providerInfo.processName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }
}
