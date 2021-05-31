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
public class StubActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("StubActivity");
        TextView tv = new TextView(this);
        tv.setText("我是StubActivity，声明了那种");
        setContentView(tv);

    }

}
