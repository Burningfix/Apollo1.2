package jianqiang.com.activityhook1.activitys;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import jianqiang.com.activityhook1.StringConstant;

/**
 * @author weishu
 * @date 16/3/29
 */
public class TargetActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("目标页面");
        TextView tv = new TextView(this);
        tv.setText("我是目标页面，未声明在xml中的那种");
        setContentView(tv);
    }

    private static final String TAG = "sanbo.TargetActivity";

    private void logd(String info) {
        Log.println(Log.DEBUG, TAG, info);
    }

    private void logi(String info) {
        Log.println(Log.INFO, TAG, info);
    }
}
