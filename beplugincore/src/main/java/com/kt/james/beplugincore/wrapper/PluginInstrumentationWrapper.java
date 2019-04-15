package com.kt.james.beplugincore.wrapper;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.app.Fragment;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.UserHandle;

import com.kt.james.beplugincore.PluginInjector;
import com.kt.james.beplugincore.PluginIntentResolver;
import com.kt.james.beplugincore.PluginLauncher;
import com.kt.james.beplugincore.PluginLoader;
import com.kt.james.beplugincore.android.HackInstrumentation;
import com.kt.james.beplugincore.content.PluginInfo;
import com.kt.james.beplugincore.manager.PluginManagerProviderClient;
import com.kt.james.beplugincore.util.ProcessUtil;

import java.util.Iterator;
import java.util.Set;

/**
 * author: James
 * 2019/4/12 21:44
 * version: 1.0
 */
public class PluginInstrumentationWrapper extends Instrumentation {

    private static final String RELAUNCH_FLAG = "relaunch.category.";

    private Instrumentation real;

    private HackInstrumentation hackInstrumentation;

    public PluginInstrumentationWrapper(Instrumentation instrumentation) {
        real = instrumentation;
        hackInstrumentation = new HackInstrumentation(instrumentation);
    }

    @Override
    public void callApplicationOnCreate(Application app) {
        real.callApplicationOnCreate(app);
    }

    public ActivityResult execStartActivity(Context who, IBinder contextThread, IBinder token, Activity target,
                                            Intent intent, int requestCode, Bundle options) {
        PluginIntentResolver.resolveActivity(intent);
        return hackInstrumentation.execStartActivity(who, contextThread, token,
                target, intent, requestCode, options);
    }

    public void execStartActivities(Context who, IBinder contextThread, IBinder token, Activity target,
                                    Intent[] intents, Bundle options) {

        PluginIntentResolver.resolveActivity(intents);

        hackInstrumentation.execStartActivities(who, contextThread, token, target, intents, options);
    }

    @Override
    public Application newApplication(ClassLoader cl, String className, Context context) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        if (ProcessUtil.isPluginProcess()) {
            PluginInfo pluginInfo = PluginManagerProviderClient.queryPluginInfoByClazzName(className);
            if (pluginInfo != null) {
                return PluginLauncher.getInstance().getRunningPlugin(pluginInfo.getPackageName()).pluginApplication;
            }
        }
        return real.newApplication(cl, className, context);
    }

    //这个B方法判断分支太多了，得重构一下
    @Override
    public Activity newActivity(ClassLoader cl, String className, Intent intent) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        if (ProcessUtil.isPluginProcess()) {
            if (PluginManagerProviderClient.isStub(className)) {
                String action = intent.getAction();
                if (action != null && action.contains(PluginIntentResolver.CLASS_SEPARATOR)) {
                    String[] data = action.split(PluginIntentResolver.CLASS_SEPARATOR);
                    String pluginClazz = data[0];
                    PluginInfo pluginInfo = PluginManagerProviderClient.queryPluginInfoByClazzName(pluginClazz);
                    if (pluginInfo != null) {
                        boolean isRunning = PluginLauncher.getInstance().isRunning(pluginInfo.getPackageName());
                        if (!isRunning) {
                            //这里做一个引导页懒加载
                        }
                    }

                    Class clazz = PluginLoader.loadPluginClassByName(pluginInfo, pluginClazz);
                    if (clazz != null) {
                        className = pluginClazz;
                        cl = clazz.getClassLoader();
                        intent.setExtrasClassLoader(clazz.getClassLoader());
                        if (data.length > 1) {
                            intent.setAction(data[1]);
                        } else {
                            intent.setAction(null);
                        }
                        //添加一个标记，用于解决activity重建的情况
                        intent.addCategory(RELAUNCH_FLAG + className);
                    } else {
                        //找不到了。。。后面再写把
                    }
                } else {
                    //进入这个分支的场景是，这个是插件的Activity，但是它因为屏幕旋转等因素重建了
                    //因为第一newActivity的时候已经把action还原了
                    // 所以这里用上面添加的Category把className拿出来，再还原一次
                    Set<String> categories = intent.getCategories();
                    if (categories != null) {
                        Iterator<String> iterator = categories.iterator();
                        while (iterator.hasNext()) {
                            String cate = iterator.next();
                            if (cate.startsWith(RELAUNCH_FLAG)) {
                                className = cate.replace(RELAUNCH_FLAG, "");
                                PluginInfo pluginInfo = PluginManagerProviderClient.queryPluginInfoByClazzName(className);
                                if (pluginInfo != null) {
                                    boolean isRunning = PluginLauncher.getInstance().isRunning(pluginInfo.getPackageName());
                                    if (!isRunning) {
                                        //懒加载页面
                                    }
                                    Class clazz = PluginLoader.loadPluginClassByName(pluginInfo, className);
                                    if (clazz != null) {
                                        cl = clazz.getClassLoader();
                                        intent.setExtrasClassLoader(clazz.getClassLoader());
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }
        try {
            return real.newActivity(cl, className, intent);
        }catch (ClassNotFoundException e) {
            throw new ClassNotFoundException("className: " + className);
        }
    }

    @Override
    public boolean onException(Object obj, Throwable e) {
        return real.onException(obj, e);
    }

    @Override
    public void callActivityOnCreate(Activity activity, Bundle icicle) {

        PluginInjector.injectActivityContext(activity);

        Intent intent = activity.getIntent();
        if (intent != null) {
            intent.setExtrasClassLoader(activity.getClassLoader());
        }
        if (icicle != null) {
            icicle.setClassLoader(activity.getClassLoader());
        }
        real.callActivityOnCreate(activity, icicle);
    }

    @Override
    public void callActivityOnDestroy(Activity activity) {
        real.callActivityOnDestroy(activity);
    }

    @Override
    public void callActivityOnSaveInstanceState(Activity activity, Bundle outState) {
        if (outState != null) {
            outState.setClassLoader(activity.getClassLoader());
        }
        real.callActivityOnSaveInstanceState(activity, outState);
    }

    @Override
    public void callActivityOnRestoreInstanceState(Activity activity, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            savedInstanceState.setClassLoader(activity.getClassLoader());
        }
        real.callActivityOnRestoreInstanceState(activity, savedInstanceState);
    }

    @Override
    public void callActivityOnPostCreate(Activity activity, Bundle icicle) {
        if (icicle != null) {
            icicle.setClassLoader(activity.getClassLoader());
        }

        real.callActivityOnPostCreate(activity, icicle);
    }

    @Override
    public void callActivityOnNewIntent(Activity activity, Intent intent) {
        if (intent != null) {
            intent.setExtrasClassLoader(activity.getClassLoader());
        }

        real.callActivityOnNewIntent(activity, intent);
    }

    @Override
    public void callActivityOnStart(Activity activity) {
        real.callActivityOnStart(activity);
    }

    @Override
    public void callActivityOnRestart(Activity activity) {
        real.callActivityOnRestart(activity);
    }

    @Override
    public void callActivityOnResume(Activity activity) {
        real.callActivityOnResume(activity);
    }

    @Override
    public void callActivityOnStop(Activity activity) {
        real.callActivityOnStop(activity);
    }

    @Override
    public void callActivityOnPause(Activity activity) {
        real.callActivityOnPause(activity);
    }

    @Override
    public void callActivityOnUserLeaving(Activity activity) {
        real.callActivityOnUserLeaving(activity);
    }

    public void execStartActivitiesAsUser(Context who, IBinder contextThread, IBinder token, Activity target,
                                          Intent[] intents, Bundle options, int userId) {

        PluginIntentResolver.resolveActivity(intents);

        hackInstrumentation.execStartActivitiesAsUser(who, contextThread, token, target, intents, options, userId);
    }

    public ActivityResult execStartActivity(Context who, IBinder contextThread, IBinder token,
                                            Fragment target, Intent intent, int requestCode, Bundle options) {

        PluginIntentResolver.resolveActivity(intent);

        return hackInstrumentation.execStartActivity(who, contextThread, token, target, intent, requestCode, options);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public ActivityResult execStartActivity(Context who, IBinder contextThread, IBinder token, Activity target,
                                            Intent intent, int requestCode, Bundle options, UserHandle user) {

        PluginIntentResolver.resolveActivity(intent);

        return hackInstrumentation.execStartActivity(who, contextThread, token, target, intent, requestCode, options, user);
    }


    /////////////  Android 4.0.4及以下  ///////////////

    public ActivityResult execStartActivity(
            Context who, IBinder contextThread, IBinder token, Activity target,
            Intent intent, int requestCode) {

        PluginIntentResolver.resolveActivity(intent);

        return hackInstrumentation.execStartActivity(who, contextThread, token, target, intent, requestCode);
    }

    public void execStartActivities(Context who, IBinder contextThread,
                                    IBinder token, Activity target, Intent[] intents) {
        PluginIntentResolver.resolveActivity(intents);

        hackInstrumentation.execStartActivities(who, contextThread, token, target, intents);
    }

    public ActivityResult execStartActivity(
            Context who, IBinder contextThread, IBinder token, Fragment target,
            Intent intent, int requestCode) {

        PluginIntentResolver.resolveActivity(intent);

        return hackInstrumentation.execStartActivity(who, contextThread, token, target, intent, requestCode);
    }

    /////// For Android 5.1
    public ActivityResult execStartActivityAsCaller(
            Context who, IBinder contextThread, IBinder token, Activity target,
            Intent intent, int requestCode, Bundle options, int userId) {
        PluginIntentResolver.resolveActivity(intent);

        return hackInstrumentation.execStartActivityAsCaller(who, contextThread, token, target, intent, requestCode, options, userId);
    }

    public void execStartActivityFromAppTask(
            Context who, IBinder contextThread, Object appTask,
            Intent intent, Bundle options) {

        PluginIntentResolver.resolveActivity(intent);

        hackInstrumentation.execStartActivityFromAppTask(who, contextThread, appTask, intent, options);
    }

    //7.1?
    public ActivityResult execStartActivityAsCaller(Context who, IBinder contextThread, IBinder token, Activity target,
                                                    Intent intent, int requestCode, Bundle options, boolean ignoreTargetSecurity,
                                                    int userId) {

        PluginIntentResolver.resolveActivity(intent);

        return hackInstrumentation.execStartActivityAsCaller(who, contextThread, token, target, intent, requestCode, options, ignoreTargetSecurity, userId);
    }


}
