package com.kt.james.beplugincore.processor;

import com.kt.james.beplugincore.BePluginGlobal;
import com.kt.james.beplugincore.content.PluginInfo;
import com.kt.james.beplugincore.manager.PluginManagerProviderClient;

import java.util.List;

/**
 * author: James
 * 2019/4/10 23:48
 * version: 1.0
 */
public class PluginMappingService {

    public static synchronized boolean isStub(String className) {
        List<StubMappingProcessor> list = BePluginGlobal.getMappingProcessor();
        for (StubMappingProcessor processor : list) {
            if (processor.isStub(className)) {
                return true;
            }
        }
        return false;
    }

    public static synchronized String bindStub(String pluginClassName, String packageName, int type) {
        List<StubMappingProcessor> list = BePluginGlobal.getMappingProcessor();
        for (StubMappingProcessor processor : list) {
            if (processor.getType() == type) {
                PluginInfo pluginInfo = PluginManagerProviderClient.queryPluginInfoByPackageName(packageName);
                if (pluginInfo == null) {
                    return null;
                }
                return processor.bindStub(pluginInfo, pluginClassName);
            }
        }
        return null;
    }

    public static synchronized void unbindStubActivity(String stubActivity, String pluginActivity, int type) {
        List<StubMappingProcessor> list = BePluginGlobal.getMappingProcessor();
        for (StubMappingProcessor processor : list) {
            if (processor.getType() == type) {
                processor.unbind(stubActivity, pluginActivity);
            }
        }
    }

}
