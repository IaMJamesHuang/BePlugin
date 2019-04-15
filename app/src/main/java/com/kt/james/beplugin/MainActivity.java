package com.kt.james.beplugin;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.kt.james.beplugincore.manager.PluginManagerProviderClient;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //这里先让plugin进程启动起来，方便debug
        PluginManagerProviderClient.queryAllInstallPlugins();
    }

    public void onJumpToPlugin(View view) {
        Intent intent = new Intent();
        intent.setAction("plugin.test");
        startActivity(intent);
    }

    public void onInstallPlugins(View view) {
        AppPluginLoader.loadPlugins();
    }

}
