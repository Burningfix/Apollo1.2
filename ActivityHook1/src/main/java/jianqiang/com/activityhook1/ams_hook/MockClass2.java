package jianqiang.com.activityhook1.ams_hook;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.lang.reflect.Proxy;
import java.util.List;

import jianqiang.com.activityhook1.RefInvoke;

/**
 * @author weishu
 * @date 16/1/7
 */
/* package */ class MockClass2 implements Handler.Callback {

    Handler mBase;

    public MockClass2(Handler base) {
        mBase = base;
    }

    @Override
    public boolean handleMessage(Message msg) {
        Log.i("sanbo.MockClass2", "handleMessage what: " + msg.what);

        // support android 8
        switch (msg.what) {
            // ActivityThread里面 "LAUNCH_ACTIVITY" 这个字段的值是100
            // 本来使用反射的方式获取最好, 这里为了简便直接使用硬编码
            // ActivityThread里面 "LAUNCH_ACTIVITY" 这个字段的值是100
            // 本来使用反射的方式获取最好, 这里为了简便直接使用硬编码
            case 100:   //for API 28以下
                handleLaunchActivity(msg);
                break;
            case 159:   //for API 28
                handleActivity(msg);
                break;
        }

        mBase.handleMessage(msg);
        return true;
    }

    private void handleActivity(Message msg) {
        // 这里简单起见,直接取出TargetActivity;
        Object obj = msg.obj;

        //android.app.servertransaction.ClientTransaction
        Log.i("sanbo.mock2", "handleActivity...obj: " + obj.toString());
        List<Object> mActivityCallbacks = (List<Object>) RefInvoke.getFieldObject(obj, "mActivityCallbacks");
        if (mActivityCallbacks.size() > 0) {
            // 新版本 更新多个Item
            // android.app.servertransaction.LaunchActivityItem
            // android.app.servertransaction.DestroyActivityItem
            // android.app.servertransaction.PauseActivityItem
            // android.app.servertransaction.ResumeActivityItem
            // android.app.servertransaction.StopActivityItem
            String className = "android.app.servertransaction.LaunchActivityItem";
            if (mActivityCallbacks.get(0).getClass().getCanonicalName().equals(className)) {
                Object object = mActivityCallbacks.get(0);
                Intent intent = (Intent) RefInvoke.getFieldObject(object, "mIntent");
                Intent target = intent.getParcelableExtra(AMSHookHelper.EXTRA_TARGET_INTENT);
                intent.setComponent(target.getComponent());


                /**
                 * 其实不要也能生效。
                 */
                //修改packageName，这样缓存才能命中
                ActivityInfo activityInfo = (ActivityInfo) RefInvoke.getFieldObject(object, "mInfo");
                Log.i("sanbo.activityInfo", "activityInfo:" + activityInfo);
                realLaunch(target, activityInfo);
            }
        }
    }


    private void handleLaunchActivity(Message msg) {
        // 这里简单起见,直接取出TargetActivity;

        Object obj = msg.obj;

        // 把替身恢复成真身
        Intent raw = (Intent) RefInvoke.getFieldObject(obj.getClass(), obj, "intent");

        Intent target = raw.getParcelableExtra(AMSHookHelper.EXTRA_TARGET_INTENT);
        raw.setComponent(target.getComponent());


        //android.app.ActivityThread$ActivityClientRecord
        Log.i("sanbo.Mock2", "obj: " + obj.getClass().toString());
        //修改packageName，这样缓存才能命中
        ActivityInfo activityInfo = (ActivityInfo) RefInvoke.getFieldObject(obj.getClass(), obj, "activityInfo");
        realLaunch(target, activityInfo);
    }

    private void realLaunch(Intent target, ActivityInfo activityInfo) {


        try {
            Log.i("sanbo.realLaunch", "=============================================");
            Log.i("sanbo.realLaunch", "target: " + target.toString());
            Log.i("sanbo.realLaunch", "activityInfo: " + activityInfo.toString());

            activityInfo.applicationInfo.packageName = target.getPackage() == null ?
                    target.getComponent().getPackageName() : target.getPackage();
            hookPackageManager();
        } catch (Throwable e) {
            Log.i("sanbo.MockClass2", Log.getStackTraceString(e));

        }
    }

    private static void hookPackageManager() throws Exception {

        // 这一步是因为 initializeJavaContextClassLoader 这个方法内部无意中检查了这个包是否在系统安装
        // 如果没有安装, 直接抛出异常, 这里需要临时Hook掉 PMS, 绕过这个检查.
        Object currentActivityThread = RefInvoke.invokeStaticMethod("android.app.ActivityThread", "currentActivityThread");

        // 获取ActivityThread里面原始的 sPackageManager
        Object sPackageManager = RefInvoke.getFieldObject("android.app.ActivityThread", currentActivityThread, "sPackageManager");

        // 准备好代理对象, 用来替换原始的对象
        Class<?> iPackageManagerInterface = Class.forName("android.content.pm.IPackageManager");
        Object proxy = Proxy.newProxyInstance(iPackageManagerInterface.getClassLoader(),
                new Class<?>[]{iPackageManagerInterface},
                new MockClass3(sPackageManager));

        // 1. 替换掉ActivityThread里面的 sPackageManager 字段
        RefInvoke.setFieldObject("android.app.ActivityThread", currentActivityThread, "sPackageManager", proxy);
    }
}
