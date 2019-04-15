package com.kt.james.beplugincore.content;

import android.content.pm.ActivityInfo;

import java.io.Serializable;

/**
 * author: James
 * 2019/4/10 22:38
 * version: 1.0
 */
public class PluginActivityInfo implements Serializable {

    private String name;

    private String launchMode = String.valueOf(ActivityInfo.LAUNCH_MULTIPLE);

    private String windowSoftInputMode;

    private String hardwareAccelerated;

    private String screenOrientation;

    private String theme;

    private String immersive;

    private String uiOptions;

    private int configChanges;

    private boolean useHostPackageName = false;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLaunchMode() {
        return launchMode;
    }

    public void setLaunchMode(String launchMode) {
        this.launchMode = launchMode;
    }

    public String getWindowSoftInputMode() {
        return windowSoftInputMode;
    }

    public void setWindowSoftInputMode(String windowSoftInputMode) {
        this.windowSoftInputMode = windowSoftInputMode;
    }

    public String getHardwareAccelerated() {
        return hardwareAccelerated;
    }

    public void setHardwareAccelerated(String hardwareAccelerated) {
        this.hardwareAccelerated = hardwareAccelerated;
    }

    public String getScreenOrientation() {
        return screenOrientation;
    }

    public void setScreenOrientation(String screenOrientation) {
        this.screenOrientation = screenOrientation;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getImmersive() {
        return immersive;
    }

    public void setImmersive(String immersive) {
        this.immersive = immersive;
    }

    public String getUiOptions() {
        return uiOptions;
    }

    public void setUiOptions(String uiOptions) {
        this.uiOptions = uiOptions;
    }

    public int getConfigChanges() {
        return configChanges;
    }

    public void setConfigChanges(int configChanges) {
        this.configChanges = configChanges;
    }

    public boolean isUseHostPackageName() {
        return useHostPackageName;
    }

    public void setUseHostPackageName(boolean useHostPackageName) {
        this.useHostPackageName = useHostPackageName;
    }
}
