package com.kt.james.independentplugin;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * author: James
 * 2019/4/14 13:57
 * version: 1.0
 */
public class PluginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plugin);
    }

    public void processJump(View view) {
        Intent intent = new Intent(this, Plugin2Activity.class);
        startActivity(intent);
    }

}
