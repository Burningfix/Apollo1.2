package jianqiang.com.activityhook1;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

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
        tv.setText("StubActivity");
        setContentView(tv);

        Log.d("sanbo.StubActivity", String.valueOf(StringConstant.string1));
    }
}
