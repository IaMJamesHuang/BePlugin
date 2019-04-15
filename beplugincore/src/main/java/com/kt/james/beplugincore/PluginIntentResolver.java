package com.kt.james.beplugincore;

import android.content.ComponentName;
import android.content.Intent;

import com.kt.james.beplugincore.content.PluginInfo;
import com.kt.james.beplugincore.manager.PluginManagerProviderClient;
import com.kt.james.beplugincore.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * author: James
 * 2019/4/13 0:02
 * version: 1.0
 */
public class PluginIntentResolver {

    public static final String CLASS_SEPARATOR = "@";//字符串越短,判断时效率越高

    public static void resolveActivity(Intent[] intents) {

    }

    public static void resolveActivity(Intent intent) {
        List<String> matchClazzName= matchPlugin(intent, PluginInfo.ACTIVITY);
        if (matchClazzName !=null && matchClazzName.size() > 0) {
            //匹配到多个Activity的情况，只取第一个
            String targetName = matchClazzName.get(0);
            PluginInfo pluginInfo = PluginManagerProviderClient.queryPluginInfoByClazzName(targetName);
            //是不是要检查一下插件有没有wakeup
            //这个空指针应该不用管
            if (pluginInfo != null) {
                String stubActivityName = PluginManagerProviderClient.bindStubActivity(targetName, pluginInfo.getPackageName());
                if (stubActivityName == null) {
                    LogUtil.e("绑定插件Activity与Stub Activity失败", targetName, pluginInfo.getPackageName());
                } else {
                    LogUtil.d("绑定插件Activity与Stub Activity成功");
                    intent.setComponent(new ComponentName(BePluginGlobal.getHostApplication().getPackageName(), stubActivityName));
                    //这里需要一个标记来让Instumentation知道AMS回调后需要生成的Activity是否是插件的Activity，当然也可以每次去向Processor反查
                    intent.setAction(targetName + CLASS_SEPARATOR + (intent.getAction() == null ? "" : intent.getAction()));
                }
            } else {
                LogUtil.e("在已经找到匹配组件名的情况下找不到对应的插件，有可能是过程中某些数据已经丢失了");
            }
        } else {
            if (intent.getComponent() != null) {
                String packageName = intent.getComponent().getPackageName();
                PluginInfo pluginInfo = PluginManagerProviderClient.queryPluginInfoByPackageName(packageName);
                if (pluginInfo != null) {
                    LogUtil.d("没有匹配到对应的组件，但是Intent指向的包名是插件的，强行修正其指向宿主");
                    intent.setComponent(new ComponentName(BePluginGlobal.getHostApplication().getPackageName(), intent.getComponent().getClassName()));
                }
            }
        }
    }

    public static List<String> matchPlugin(Intent intent, int type) {
        List<String> result = null;
        String packageName = intent.getPackage();
        if (packageName == null && intent.getComponent() != null) {
            packageName = intent.getComponent().getPackageName();
        }
        //指定了包名
        if (packageName != null && !packageName.equals(BePluginGlobal.getHostApplication().getPackageName())) {
            PluginInfo pluginInfo = PluginManagerProviderClient.queryPluginInfoByPackageName(packageName);
            if (pluginInfo != null) {
                result = pluginInfo.matchPlugin(intent, type);
                if (result != null && result.size() > 0) {
                    LogUtil.d("此intent是插件的组件，且匹配成功");
                } else {
                    LogUtil.d("此intent是插件的组件，但匹配失败");
                }
            } else {
                LogUtil.d("此intent不是插件的组件");
            }
        } else {
            //没有指定包名，需要遍历所有安装的插件
            List<PluginInfo> plugins = PluginManagerProviderClient.queryAllInstallPlugins();
            if (plugins != null) {
                for (PluginInfo pluginInfo : plugins) {
                    List<String> matchList = pluginInfo.matchPlugin(intent, type);
                    if (matchList != null && matchList.size() > 0) {
                        if (result == null) {
                            result = new ArrayList<>();
                        }
                        result.addAll(matchList);
                    }
                }
            }

            if (result == null || result.size() == 0) {
                LogUtil.d("没有匹配到Intent，可能不是插件的组件或者插件未安装");
            } else {
                LogUtil.d("匹配插件组件成功", packageName);
            }
        }
        return result;
    }

}
