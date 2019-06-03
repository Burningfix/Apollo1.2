package jianqiang.com.activityhook1.ams_hook;

import android.content.pm.PackageInfo;
import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author weishu
 * @date 16/1/7
 */
public class MockClass3 implements InvocationHandler {

    private String TAG = "sanbo.mock3";
    private Object mBase;

    public MockClass3(Object base) {
        mBase = base;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Log.v(TAG, "invoke methodname: " + method.getName());
        if (method.getName().equals("getPackageInfo")) {
            return new PackageInfo();
        }
        return method.invoke(mBase, args);
    }
}
