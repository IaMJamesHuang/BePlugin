package com.kt.james.beplugincore.manager;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.kt.james.beplugincore.BePluginGlobal;
import com.kt.james.beplugincore.content.InstallResult;
import com.kt.james.beplugincore.content.LoadedPlugin;
import com.kt.james.beplugincore.content.PluginInfo;
import com.kt.james.beplugincore.PluginLauncher;
import com.kt.james.beplugincore.callback.PluginStatusCallback;
import com.kt.james.beplugincore.callback.PluginStatusCallbackImpl;
import com.kt.james.beplugincore.constant.ResultCodeConstant;
import com.kt.james.beplugincore.processor.PluginMappingService;
import com.kt.james.beplugincore.processor.StubMappingProcessor;
import com.kt.james.beplugincore.util.LogUtil;

import java.util.ArrayList;

/**
 * author: James
 * 2019/4/10 11:17
 * version: 1.0
 */
public class PluginManagerProvider extends ContentProvider {

    private PluginManagerService managerService;

    private PluginLauncher pluginLauncher;

    private PluginStatusCallback pluginStatusListener;

    private static Uri CONTENT_URI;

    public static final String ACTION_INSTALL = "action_install";
    public static final String RESULT_INSTALL = "result_install";

    public static final String ACTION_WAKEUP = "action_wakeup";
    public static final String RESULT_WAKEUP = "result_wakeup";

    public static final String ACTION_WAKEUP_ALL = "action_wakeup_all";
    public static final String RESULT_WAKEUP_ALL = "result_wakeup_all";

    public static final String ACTION_REMOVE = "action_remove";
    public static final String RESULT_REMOVE = "result_remove";

    public static final String ACTION_QUERY = "action_query";
    public static final String RESULT_QUERY = "result_query";

    public static final String ACTION_QUERY_ALL = "action_query_all";
    public static final String RESULT_QUERY_ALL = "result_query_all";

    public static final String ACTION_QUERY_BY_CLASS = "action_query_by_class";
    public static final String RESULT_QUERY_BY_CLASS = "result_query_by_class";

    public static final String ACTION_BIND_STUB_ACTIVITY = "action_bind_stub_activity";
    public static final String RESULT_BIND_STUB_ACTIVITY = "result_bind_stub_activity";

    public static final String ACTION_UNBIND_STUB_ACTIVITY = "action_unbind_activity";
    public static final String RESULT_UNBIND_STUB_ACTIVITY = "result_unbind_activity";

    public static final String ACTION_IS_STUB = "action_is_stub";
    public static final String RESULT_IS_STUB = "result_is_stub";

    public static Uri buildCallUri() {
        if (CONTENT_URI == null) {
            CONTENT_URI = Uri.parse("content://" + BePluginGlobal.getHostApplication().getPackageName() + ".manager" + "/call");
        }
        return CONTENT_URI;
    }

    @Override
    public boolean onCreate() {
        managerService = new PluginManagerService();
        pluginLauncher = PluginLauncher.getInstance();
        managerService.loadInstalledPlugins();
        pluginStatusListener = new PluginStatusCallbackImpl();
        return true;
    }

    @Override
    public Cursor query(@Nullable Uri uri, String[] projection,  String selection, String[] selectionArgs,  String sortOrder) {
        return getContext().getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);
    }


    @Override
    public String getType(@Nullable Uri uri) {
        return getContext().getContentResolver().getType(uri);
    }

    @Override
    public Uri insert(@Nullable Uri uri,  ContentValues values) {
        return getContext().getContentResolver().insert(uri, values);
    }

    @Override
    public int delete(@Nullable Uri uri, @Nullable String selection, String[] selectionArgs) {
        return getContext().getContentResolver().delete(uri, selection, selectionArgs);
    }

    @Override
    public int update(@Nullable Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return getContext().getContentResolver().update(uri, values, selection, selectionArgs);
    }

    @Override
    public Bundle call(@Nullable String method, @Nullable String arg, @Nullable Bundle extras) {
        LogUtil.d("PluginManager.call", method, arg);
        return dispatchCallMethod(method, arg, extras);
    }

    private Bundle dispatchCallMethod(String method, String arg, Bundle extras) {
        Bundle bundle = new Bundle();
        if (ACTION_QUERY.equals(method)) {
            PluginInfo pluginInfo = managerService.getPluginInfo(arg);
            if (pluginInfo != null) {
                bundle.putSerializable(RESULT_QUERY, pluginInfo);
            }
        } else if (ACTION_INSTALL.equals(method)) {
            InstallResult result = managerService.installPlugin(arg);
            if (result != null) {
                bundle.putSerializable(RESULT_INSTALL, result);
                pluginStatusListener.onInstall(result.getResult(), result.getPackageName(), result.getVersion());
            }
        } else if (ACTION_WAKEUP.equals(method)) {
            PluginInfo pluginInfo = managerService.getPluginInfo(arg);
            LoadedPlugin loadedPlugin = null;
            if (pluginInfo != null) {
                loadedPlugin = pluginLauncher.wakeupPlugin(pluginInfo);
                pluginStatusListener.onWakeup(pluginInfo.getPackageName());
            }
            bundle.putInt(RESULT_WAKEUP, loadedPlugin == null ? ResultCodeConstant.WAKEUP_FAIL : ResultCodeConstant.WAKEUP_SUCESS);
        } else if (ACTION_REMOVE.equals(method)) {
            //还没有写
        } else if (ACTION_QUERY_ALL.equals(method)) {
            ArrayList<PluginInfo> list = managerService.getAllPlugins();
            if (list != null) {
                bundle.putSerializable(RESULT_QUERY_ALL, list);
            }
        } else if (ACTION_QUERY_BY_CLASS.equals(method)) {
            PluginInfo pluginInfo = managerService.getPluginInfoByClazzName(arg);
            if (pluginInfo != null) {
                bundle.putSerializable(RESULT_QUERY_BY_CLASS, pluginInfo);
            }
        } else if (ACTION_BIND_STUB_ACTIVITY.equals(method)) {
            String packageName = extras.getString("packageName");
            String pluginActivityName = extras.getString("pluginActivityName");

            String stubActivityName = PluginMappingService.bindStub(pluginActivityName, packageName, StubMappingProcessor.TYPE_ACTIVITY);
            if (!TextUtils.isEmpty(stubActivityName)) {
                bundle.putString(RESULT_BIND_STUB_ACTIVITY, stubActivityName);
            }
        } else if (ACTION_IS_STUB.equals(method)) {
            bundle.putBoolean(RESULT_IS_STUB, PluginMappingService.isStub(arg));
        } else if (ACTION_UNBIND_STUB_ACTIVITY.equals(method)) {
            PluginMappingService.unbindStubActivity(arg, extras.getString("pluginActivity"), StubMappingProcessor.TYPE_ACTIVITY);
        } else if (ACTION_WAKEUP_ALL.endsWith(method)) {
            ArrayList<PluginInfo> list = managerService.getAllPlugins();
            if (list != null) {
                for (PluginInfo pluginInfo : list) {
                    pluginLauncher.wakeupPlugin(pluginInfo);
                }
            }
        }
        return bundle;
    }

}
