package com.kt.james.beplugincore.callback;

/**
 * author: James
 * 2019/4/11 15:36
 * version: 1.0
 */
public interface PluginStatusCallback {

    void onInstall(int result, String packageName, String version);

    void onWakeup(String packageName);

    void onRemove(String packageName, int code);

}
