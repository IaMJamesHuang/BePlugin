package com.kt.james.beplugin;

import android.content.res.AssetManager;

import com.kt.james.beplugincore.content.InstallResult;
import com.kt.james.beplugincore.manager.PluginManagerProvider;
import com.kt.james.beplugincore.manager.PluginManagerProviderClient;
import com.kt.james.beplugincore.util.FileUtil;
import com.kt.james.beplugincore.util.LogUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * author: James
 * 2019/4/14 13:49
 * version: 1.0
 */
public class AppPluginLoader {

    public static void loadPlugins() {
        //加载、启动插件
        AssetManager assetManager = DemoApplication.getApplication().getAssets();
        try {
            String[] fileList = assetManager.list("");
            if (fileList == null) {
                return;
            }
            for (String file : fileList) {
                if (file.endsWith(".apk")) {
                    install(file);
                }
            }
            PluginManagerProviderClient.wakeupAllPlugins();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void install(String name) {
        InputStream inputStream = null;
        try {
           inputStream = DemoApplication.getApplication().getAssets().open(name);
            File file = DemoApplication.getApplication().getExternalFilesDir(null);
            if (file == null) {
                return;
            }
            String dest = file.getAbsolutePath() + "/" + name;
            if (FileUtil.copyFile(inputStream, dest)) {
                InstallResult result = PluginManagerProviderClient.installPlugin(dest);
                if (result != null) {
                    LogUtil.d(result.getResult());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
