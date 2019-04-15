package com.kt.james.beplugincore.processor;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import com.kt.james.beplugincore.BePluginGlobal;
import com.kt.james.beplugincore.content.PluginActivityInfo;
import com.kt.james.beplugincore.content.PluginInfo;
import com.kt.james.beplugincore.util.LogUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * author: James
 * 2019/4/10 21:43
 * version: 1.0
 */
public class ActivityStubMappingProcessor implements StubMappingProcessor {

    private static HashMap<String, List<String>> standardActivityMapping = new HashMap<>();

    private static HashMap<String, List<String>> singleTaskActivityMapping = new HashMap<>();

    private static HashMap<String, List<String>> singleTopActivityMapping = new HashMap<>();

    private static HashMap<String, List<String>> singleInstanceActivityMapping = new HashMap<>();

    private static boolean isInited;

    @Override
    public int getType() {
        return StubMappingProcessor.TYPE_ACTIVITY;
    }

    @Override
    public String bindStub(PluginInfo pluginInfo, String pluginComponentClassName) {
        initMappingInfo();

        PluginActivityInfo activityInfo = pluginInfo.getPluginActivities().get(pluginComponentClassName);
        HashMap<String, List<String>> bindingMapping = null;
        int launchMode = Integer.parseInt(activityInfo.getLaunchMode());

        if (launchMode == ActivityInfo.LAUNCH_MULTIPLE) {
            bindingMapping = standardActivityMapping;
        } else if (launchMode == ActivityInfo.LAUNCH_SINGLE_TOP) {
            bindingMapping = singleTopActivityMapping;
        } else if (launchMode == ActivityInfo.LAUNCH_SINGLE_TASK) {
            bindingMapping = singleTaskActivityMapping;
        } else if (launchMode == ActivityInfo.LAUNCH_SINGLE_INSTANCE) {
            bindingMapping = singleInstanceActivityMapping;
        }

        if (bindingMapping != null) {
            Iterator<Map.Entry<String, List<String>>> iterator = bindingMapping.entrySet().iterator();
            String result = null;
            while (iterator.hasNext()) {
                Map.Entry<String, List<String>> entry = iterator.next();
                List<String> val = entry.getValue();
                if (val == null || val.size() == 0) {
                    //继续往下找，防止这个类已经绑定了
                    result = entry.getKey();
                } else {
                    //已经绑定过
                    if (val.get(0).equals(pluginComponentClassName)) {
                        return entry.getKey();
                    }
                }
            }

            if (result != null) {
                List<String> list = bindingMapping.get(result);
                if (list == null) {
                    list = new ArrayList<>();
                }
                list.add(pluginComponentClassName);
                return result;
            } else {
                //不够用了
                LogUtil.e("占位类不够用了");
            }
        }
        return null;
    }

    @Override
    public void unbind(String stubClassName, String pluginComponentClassName) {
        initMappingInfo();
        if (reduce(standardActivityMapping.get(stubClassName), pluginComponentClassName)) {
            return;
        }
        if (reduce(singleInstanceActivityMapping.get(stubClassName), pluginComponentClassName)) {
            return;
        }
        if (reduce(singleTopActivityMapping.get(stubClassName), pluginComponentClassName)) {
            return;
        }
        if (reduce(singleTaskActivityMapping.get(stubClassName), pluginComponentClassName)) {
            return;
        }
    }

    @Override
    public boolean isStub(String stubClassName) {
        initMappingInfo();
        return standardActivityMapping.containsKey(stubClassName) ||
                singleTopActivityMapping.containsKey(stubClassName) ||
                singleTaskActivityMapping.containsKey(stubClassName) ||
                singleInstanceActivityMapping.containsKey(stubClassName);
    }

    @Override
    public String getBindPluginClassName(String stubClassName) {
        String result = searchMap(standardActivityMapping, stubClassName);
        if (result == null) {
            result = searchMap(singleTopActivityMapping, stubClassName);
        }
        if (result == null) {
            result = searchMap(singleTaskActivityMapping, stubClassName);
        }
        if (result == null) {
            searchMap(singleInstanceActivityMapping, stubClassName);
        }
        if (result == null) {
            result = stubClassName;
        }
        return result;
    }

    private static boolean reduce(List<String> mapList, String pluginClassName) {
        if (mapList != null && mapList.size() > 0) {
            return mapList.remove(pluginClassName);
        }
        return false;
    }

    private static String searchMap(Map<String, List<String>> map, String target) {
        if (map != null) {
            Iterator<Map.Entry<String, List<String>>> iterator = map.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, List<String>> entry = iterator.next();
                String key = entry.getKey();
                if (target.equals(key)) {
                    List<String> val = entry.getValue();
                    if (val != null && val.size() > 0) {
                        return val.get(0);
                    } else {
                        return null;
                    }
                }
            }
        }
        return null;
    }

    private static void
    initMappingInfo() {
        if (isInited) {
            return;
        }
        loadStubActivity();
        isInited = true;
    }

    private static void loadStubActivity() {
        Intent queryIntent = new Intent();
        queryIntent.setAction(buildDefaultAction());
        queryIntent.setPackage(getPackageName());

        List<ResolveInfo> list = BePluginGlobal.getHostApplication().getPackageManager().queryIntentActivities(queryIntent, PackageManager.MATCH_DEFAULT_ONLY);

        if (list != null && list.size() > 0) {
            for (ResolveInfo info : list) {
                if (info.activityInfo.launchMode == ActivityInfo.LAUNCH_SINGLE_TOP) {
                    singleTopActivityMapping.put(info.activityInfo.name, null);
                } else if (info.activityInfo.launchMode == ActivityInfo.LAUNCH_SINGLE_TASK) {
                    singleTaskActivityMapping.put(info.activityInfo.name, null);
                } else if (info.activityInfo.launchMode == ActivityInfo.LAUNCH_SINGLE_INSTANCE) {
                    singleInstanceActivityMapping.put(info.activityInfo.name, null);
                } else {
                    standardActivityMapping.put(info.activityInfo.name, null);
                }
            }
        }
    }

    private static String buildDefaultAction() {
        return BePluginGlobal.getHostApplication().getPackageName() + ".STUB_DEFAULT";
    }

    private static String getPackageName() {
        return BePluginGlobal.getHostApplication().getPackageName();
    }

}
