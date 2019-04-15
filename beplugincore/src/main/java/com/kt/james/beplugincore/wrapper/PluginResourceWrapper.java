package com.kt.james.beplugincore.wrapper;

import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import com.kt.james.beplugincore.content.PluginInfo;

/**
 * author: James
 * 2019/4/12 16:53
 * version: 1.0
 */
public class PluginResourceWrapper extends Resources {

    public PluginResourceWrapper(AssetManager assets, DisplayMetrics metrics,
                                 Configuration config, PluginInfo pluginInfo) {
        super(assets, metrics, config);
    }
}
