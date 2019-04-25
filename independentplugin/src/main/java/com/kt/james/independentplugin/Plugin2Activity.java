package com.kt.james.independentplugin;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * author: James
 * 2019/4/15 10:41
 * version: 1.0
 */
public class Plugin2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plugin2);
        TextView tv = findViewById(R.id.tv_text);
        String thing = getClassLoader().getClass().getSimpleName();
        tv.setText(thing);
        TextView textView = new TextView(this);
        textView.setText(thing);
        LinearLayout layout = findViewById(R.id.ll_container);
        layout.addView(textView);
    }
}
