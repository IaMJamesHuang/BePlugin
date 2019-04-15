package com.kt.james.beplugincore.android;

import android.view.View;

import com.kt.james.beplugincore.util.LogUtil;
import com.kt.james.beplugincore.util.ReflectInvoker;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * author: James
 * 2019/4/14 20:00
 * version: 1.0
 */
public class CompatForSupportv7ViewInflater {

    private static final String android_support_v7_app_AppCompatViewInflater = "android.support.v7.app.AppCompatViewInflater";
    private static final String android_support_v7_app_AppCompatViewInflater_sConstructorMap = "sConstructorMap";

    public static void installPluginCustomViewConstructorCache() {
        Class AppCompatViewInflater = null;
        try {
            AppCompatViewInflater = Class.forName(android_support_v7_app_AppCompatViewInflater);
            Map cache = (Map) ReflectInvoker.getField(null, AppCompatViewInflater,
                    android_support_v7_app_AppCompatViewInflater_sConstructorMap);
            if (cache != null) {
                EmptyHashMap<String, Constructor<? extends View>> newCacheMap = new EmptyHashMap<String, Constructor<? extends View>>();
                newCacheMap.putAll(cache);
                ReflectInvoker.setField(null, AppCompatViewInflater,
                        android_support_v7_app_AppCompatViewInflater_sConstructorMap, newCacheMap);
            }
        } catch (ClassNotFoundException e) {
            LogUtil.printException("CompatForSupportv7ViewInflater.installPluginCustomViewConstructorCache", e);
        }
    }

    public static class EmptyHashMap<K, V> extends HashMap<K, V> {

        @Override
        public V put(K key, V value) {
            //不缓存
            return super.put(key, null);
        }

    }

}
