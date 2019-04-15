package com.kt.james.beplugincore.content;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.util.SparseArray;

import com.kt.james.beplugincore.BePluginGlobal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * author: James
 * 2019/4/10 21:19
 * version: 1.0
 */
public class PluginInfo implements Serializable {

    private static final long serialVersionUID = 0x01;

    public static final int ACTIVITY = 0;

    private String applicationName;

    private String label;

    private String packageName;

    private String platformBuildVersionCode;

    private String platformBuildVersionName;

    private String minSdkVersion;

    private String targetSdkVersion;

    private String versionCode;

    private String versionName;

    private String requiredHostVersionName;

    private String requiredHostVersionCode;

    private String installPath;

    private boolean isStandalone;

    private boolean autoStart;

    private long installationTime;

    private int applicationIcon;

    private int applicationLogo;

    private int applicationTheme;

    private HashMap<String, PluginActivityInfo> pluginActivities;

    private HashMap<String, List<PluginIntentFilter>> activityIntentFilters;

    private transient SparseArray<PackageInfo> packageInfo;

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getPlatformBuildVersionCode() {
        return platformBuildVersionCode;
    }

    public void setPlatformBuildVersionCode(String platformBuildVersionCode) {
        this.platformBuildVersionCode = platformBuildVersionCode;
    }

    public String getPlatformBuildVersionName() {
        return platformBuildVersionName;
    }

    public void setPlatformBuildVersionName(String platformBuildVersionName) {
        this.platformBuildVersionName = platformBuildVersionName;
    }

    public String getMinSdkVersion() {
        return minSdkVersion;
    }

    public void setMinSdkVersion(String minSdkVersion) {
        this.minSdkVersion = minSdkVersion;
    }

    public String getTargetSdkVersion() {
        return targetSdkVersion;
    }

    public void setTargetSdkVersion(String targetSdkVersion) {
        this.targetSdkVersion = targetSdkVersion;
    }

    public String getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(String versionCode) {
        this.versionCode = versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public boolean isStandalone() {
        return isStandalone;
    }

    public void setStandalone(boolean standalone) {
        isStandalone = standalone;
    }

    public HashMap<String, PluginActivityInfo> getPluginActivities() {
        return pluginActivities;
    }

    public void setPluginActivities(HashMap<String, PluginActivityInfo> pluginActivities) {
        this.pluginActivities = pluginActivities;
    }

    public boolean isAutoStart() {
        return autoStart;
    }

    public void setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
    }

    public String getRequiredHostVersionName() {
        return requiredHostVersionName;
    }

    public void setRequiredHostVersionName(String requiredHostVersionName) {
        this.requiredHostVersionName = requiredHostVersionName;
    }

    public String getRequiredHostVersionCode() {
        return requiredHostVersionCode;
    }

    public void setRequiredHostVersionCode(String requiredHostVersionCode) {
        this.requiredHostVersionCode = requiredHostVersionCode;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public HashMap<String, List<PluginIntentFilter>> getActivityIntentFilters() {
        return activityIntentFilters;
    }

    public void setActivityIntentFilters(HashMap<String, List<PluginIntentFilter>> activityIntentFilters) {
        this.activityIntentFilters = activityIntentFilters;
    }

    public String getInstallPath() {
        return installPath;
    }

    public void setInstallPath(String installPath) {
        this.installPath = installPath;
    }

    public long getInstallationTime() {
        return installationTime;
    }

    public void setInstallationTime(long installationTime) {
        this.installationTime = installationTime;
    }

    public int getApplicationIcon() {
        return applicationIcon;
    }

    public void setApplicationIcon(int applicationIcon) {
        this.applicationIcon = applicationIcon;
    }

    public int getApplicationLogo() {
        return applicationLogo;
    }

    public void setApplicationLogo(int applicationLogo) {
        this.applicationLogo = applicationLogo;
    }

    public int getApplicationTheme() {
        return applicationTheme;
    }

    public void setApplicationTheme(int applicationTheme) {
        this.applicationTheme = applicationTheme;
    }

    public PackageInfo getPackageInfo(Integer flag) {
        if (packageInfo == null) {
            packageInfo = new SparseArray<>();
        }
        PackageInfo info = packageInfo.get(flag);
        if (info == null) {
            info = BePluginGlobal.getHostApplication().getPackageManager().getPackageArchiveInfo(getInstallPath(), flag);
            if (info != null && info.applicationInfo != null) {
                info.applicationInfo.sourceDir = getInstallPath();
                info.applicationInfo.publicSourceDir = getInstallPath();
            }
            packageInfo.put(flag, info);
        }
        return info;
    }

    public List<String> matchPlugin(Intent intent, int type) {
        List<String> result = null;
        //显式启动
        if (intent.getComponent() != null) {
            if (containsClazzName(intent.getComponent().getClassName())) {
                String clazz = intent.getComponent().getClassName();
                result = new ArrayList<>(1);
                result.add(clazz);
                return result;
            }
        } else {
            //隐式启动，即通过IntentFilter
            if (type == ACTIVITY) {
                result = findClazzByIntent(intent, getActivityIntentFilters());
                return result;
            }
        }
        return null;
    }

    private static List<String> findClazzByIntent(Intent intent, HashMap<String, List<PluginIntentFilter>> intentFilter) {
        if (intentFilter != null) {
            List<String> list = null;
            Iterator<Map.Entry<String, List<PluginIntentFilter>>> iterator = intentFilter.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, List<PluginIntentFilter>> item = iterator.next();
                Iterator<PluginIntentFilter> values = item.getValue().iterator();
                while (values.hasNext()) {
                    PluginIntentFilter filter = values.next();
                    int result = filter.match(intent.getAction(), intent.getType(), intent.getScheme(),
                            intent.getData(), intent.getCategories());
                    if (result != PluginIntentFilter.NO_MATCH_ACTION &&
                        result != PluginIntentFilter.NO_MATCH_CATEGORY &&
                        result != PluginIntentFilter.NO_MATCH_TYPE &&
                        result != PluginIntentFilter.NO_MATCH_DATA) {
                        if (list == null) {
                            list = new ArrayList<>();
                        }
                        list.add(item.getKey());
                    }
                }
            }
            return list;
        }
        return null;
    }

    public boolean containsClazzName(String clazzName) {
        if (getPluginActivities().containsKey(clazzName)) {
            return true;
        }
        return false;
    }

}
