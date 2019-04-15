package com.kt.james.beplugincore;

import android.app.Application;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.content.res.XmlResourceParser;
import android.text.TextUtils;

import com.kt.james.beplugincore.android.HackAssetManager;
import com.kt.james.beplugincore.content.PluginActivityInfo;
import com.kt.james.beplugincore.content.PluginInfo;
import com.kt.james.beplugincore.content.PluginIntentFilter;
import com.kt.james.beplugincore.util.LogUtil;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * author: James
 * 2019/4/11 16:36
 * version: 1.0
 */
public class PluginManifestParser {

    public static PluginInfo parserPluginManifest(String src) {
        try {
            AssetManager assetManager = AssetManager.class.newInstance();
            new HackAssetManager(assetManager).addAssetPath(src);

            XmlResourceParser parser = assetManager.openXmlResourceParser("AndroidManifest.xml");

            String namespaceAndroid = null;
            String packageName = null;
            PluginInfo pluginInfo = new PluginInfo();

            int eventType = parser.getEventType();
            do {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT: {
                        break;
                    }
                    case XmlPullParser.START_TAG: {
                        String tag = parser.getName();
                        if ("manifest".equals(tag)) {
                            namespaceAndroid = parser.getAttributeNamespace(0);
                            if (TextUtils.isEmpty(namespaceAndroid)) {
                                namespaceAndroid = "http://schemas.android.com/apk/res/android";
                            }
                            packageName = parser.getAttributeValue(null, "package");
                            String versionCode = parser.getAttributeValue(namespaceAndroid, "versionCode");
                            String versionName = parser.getAttributeValue(namespaceAndroid, "versionName");
                            String platformBuildVersionCode = parser.getAttributeValue(null, "platformBuildVersionCode");
                            String platformBuildVersionName = parser.getAttributeValue(null, "platformBuildVersionName");

                            //用于标志是否为独立插件
                            //当这个值等于宿主程序packageName时，则认为这个插件是需要依赖宿主的class和resource的
                            String hostApplicationId = parser.getAttributeValue(null, "hostApplicationId");
                            if (hostApplicationId == null) {
                                hostApplicationId = parser.getAttributeValue(namespaceAndroid, "sharedUserId");
                            }

                            //非独立插件依赖宿主的版本号
                            String requiredHostVersionName = parser.getAttributeValue(null, "requiredHostVersionName");
                            String requiredHostVersionCode = parser.getAttributeValue(null, "requiredHostVersionCode");

                            String autoStart = parser.getAttributeValue(null, "autoStart");

                            pluginInfo.setPackageName(packageName);
                            pluginInfo.setVersionCode(versionCode);
                            pluginInfo.setVersionName(versionName);
                            pluginInfo.setPlatformBuildVersionCode(platformBuildVersionCode);
                            pluginInfo.setPlatformBuildVersionName(platformBuildVersionName);
                            pluginInfo.setStandalone(hostApplicationId == null || !BePluginGlobal.getHostApplication().getPackageName().equals(hostApplicationId));
                            pluginInfo.setAutoStart("true".equals(autoStart));
                            if (!pluginInfo.isStandalone()) {
                                pluginInfo.setRequiredHostVersionCode(requiredHostVersionCode);
                                pluginInfo.setRequiredHostVersionName(requiredHostVersionName);
                            }

                            LogUtil.d(packageName + " " + versionCode + " " + versionName + " " + hostApplicationId + " " + BePluginGlobal.getHostApplication().getPackageName());

                        } else if ("uses-sdk".equals(tag)) {
                            String minSdkVersion = parser.getAttributeValue(namespaceAndroid, "minSdkVersion");
                            String targetSdkVersion = parser.getAttributeValue(namespaceAndroid, "targetSdkVersion");

                            pluginInfo.setMinSdkVersion(minSdkVersion);
                            pluginInfo.setTargetSdkVersion(targetSdkVersion);
                        } else if ("application".equals(tag)) {
                            String label = parser.getAttributeValue(namespaceAndroid, "label");
                            String applicationName = parser.getAttributeValue(namespaceAndroid, "name");
                            if (applicationName == null) {
                                applicationName = Application.class.getName();
                            }
                            applicationName = getFullName(applicationName, packageName);

                            pluginInfo.setApplicationName(applicationName);
                            pluginInfo.setLabel(label);

                            LogUtil.d("applicationName: " +  applicationName + "label" +  label);
                        } else if ("activity".equals(tag)) {
                            String windowSoftInputMode = parser.getAttributeValue(namespaceAndroid, "windowSoftInputMode");//strin
                            String hardwareAccelerated = parser.getAttributeValue(namespaceAndroid, "hardwareAccelerated");//int string
                            String launchMode = parser.getAttributeValue(namespaceAndroid, "launchMode");//string
                            String screenOrientation = parser.getAttributeValue(namespaceAndroid, "screenOrientation");//string
                            String theme = parser.getAttributeValue(namespaceAndroid, "theme");//int
                            String immersive = parser.getAttributeValue(namespaceAndroid, "immersive");//int string
                            String uiOptions = parser.getAttributeValue(namespaceAndroid, "uiOptions");//int string
                            String configChanges = parser.getAttributeValue(namespaceAndroid, "configChanges");//int string
                            String activityName = parser.getAttributeValue(namespaceAndroid, "name");

                            HashMap<String, List<PluginIntentFilter>> intentMap = pluginInfo.getActivityIntentFilters();
                            if (intentMap == null) {
                                intentMap = new HashMap<>();
                                pluginInfo.setActivityIntentFilters(intentMap);
                            }
                            addIntentFilter(intentMap, packageName, namespaceAndroid, parser, "activity");

                            HashMap<String, PluginActivityInfo> activityInfos = pluginInfo.getPluginActivities();
                            if (activityInfos == null) {
                                activityInfos = new HashMap<>();
                                pluginInfo.setPluginActivities(activityInfos);
                            }

                            PluginActivityInfo activityInfo = activityInfos.get(activityName);
                            if (activityInfo == null) {
                                activityInfo = new PluginActivityInfo();
                                activityInfos.put(activityName, activityInfo);
                            }

                            activityInfo.setHardwareAccelerated(hardwareAccelerated);
                            activityInfo.setImmersive(immersive);
                            if (launchMode == null) {
                                launchMode = String.valueOf(ActivityInfo.LAUNCH_MULTIPLE);
                            }
                            activityInfo.setLaunchMode(launchMode);
                            activityInfo.setName(activityName);
                            activityInfo.setScreenOrientation(screenOrientation);
                            activityInfo.setTheme(theme);
                            activityInfo.setWindowSoftInputMode(windowSoftInputMode);
                            activityInfo.setUiOptions(uiOptions);
                            if (configChanges != null) {
                                activityInfo.setConfigChanges((int)Long.parseLong(configChanges.replace("0x", ""), 16));
                            }
                        }
                    }
                    case XmlPullParser.END_TAG: {
                        break;
                    }
                }
                eventType = parser.next();
            } while (eventType != XmlPullParser.END_DOCUMENT);

            //有可能没有配置application节点，这里需要检查一下application
            if (pluginInfo.getApplicationName() == null) {
                pluginInfo.setApplicationName(Application.class.getName());
            }

            return pluginInfo;
        } catch (Exception e) {
            LogUtil.printException("PluginManifestParser.parserPluginManifest", e);
        }
        return null;
    }

    private static String addIntentFilter(HashMap<String, List<PluginIntentFilter>> map, String packageName,
                                          String namespace, XmlResourceParser parser, String endTag) throws XmlPullParserException, IOException {
        int eventType= parser.getEventType();
        String componentName = parser.getAttributeValue(namespace, "name");
        List<PluginIntentFilter> list = map.get(componentName);
        if (list == null) {
            list = new ArrayList<>();
            map.put(componentName, list);
        }

        PluginIntentFilter intentFilter = new PluginIntentFilter();
        do {
            switch (eventType) {
                case XmlPullParser.START_TAG: {
                    String tag = parser.getName();
                    if ("intent-filter".equals(tag)) {
                        intentFilter = new PluginIntentFilter();
                        list.add(intentFilter);
                    } else {
                        intentFilter.readFromXml(tag, namespace, parser);
                    }
                }
            }
            eventType = parser.next();
        } while (!endTag.equals(parser.getName()));

        return componentName;
    }

    private static String getFullName(String name, String packageName) {
        if (name == null) {
            return null;
        }
        StringBuilder sb = null;
        if (name.startsWith(".")) {
            sb = new StringBuilder();
            sb.append(packageName);
            sb.append(name);
        } else if (!name.contains(".")) {
            sb = new StringBuilder();
            sb.append(packageName).append(".").append(name);
        } else {
            return name;
        }
        return sb.toString();
    }

}
