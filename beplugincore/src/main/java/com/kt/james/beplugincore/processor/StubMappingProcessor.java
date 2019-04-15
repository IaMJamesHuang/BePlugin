package com.kt.james.beplugincore.processor;

import com.kt.james.beplugincore.content.PluginInfo;

/**
 * author: James
 * 2019/4/10 21:31
 * version: 1.0
 */
public interface StubMappingProcessor {

    int TYPE_ACTIVITY = 0;

    int TYPE_SERVICE = 1;

    /**
     *
     * @return 返回可以处理的组件类型
     */
    int getType();

    /**
     *
     * @param pluginInfo 插件
     * @param pluginComponentClassName 组件名称
     * @return 返回插件绑定到的组件的名称
     */
    String bindStub(PluginInfo pluginInfo, String pluginComponentClassName);

    /**
     *
     * @param stubClassName 占位组件名称
     * @param pluginComponentClassName 插件组件名称
     */
    void unbind(String stubClassName, String pluginComponentClassName);

    /**
     *
     * @param stubClassName 类名
     * @return 是否是一个占位组件
     */
    boolean isStub(String stubClassName);

    /**
     *
     * @param stubClassName 占位组件类名
     * @return 与其绑定的插件组件类名
     */
    String getBindPluginClassName(String stubClassName);

}
