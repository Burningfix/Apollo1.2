package com.example.jianqiang.testactivity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import jianqiang.com.activityhook1.StringConstant;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("PluginActivity");
        TextView tv = new TextView(this);
        tv.setText("PluginActivity");
        setContentView(tv);

        Log.d("sanbo.plugin", String.valueOf(StringConstant.string1));
    }
}
