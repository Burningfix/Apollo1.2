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
/* package */ class MockmCallback implements Handler.Callback {


    Handler mBase;

    public MockmCallback(Handler base) {
        mBase = base;
    }

    @Override
    public boolean handleMessage(Message msg) {
        logi("handleMessage what: " + msg.toString());

        // support android 8
        switch (msg.what) {
            // ActivityThread里面 "LAUNCH_ACTIVITY" 这个字段的值是100
            // 本来使用反射的方式获取最好, 这里为了简便直接使用硬编码
            case 100://for API 28以下
                handleLaunchActivity(msg);
                break;
            case 159://for API 28
                handleActivity(msg);
                break;
//            case 112: //zenus方式
//                handleNewIntent(msg);
//                break;
        }

        mBase.handleMessage(msg);
        return true;
    }

//    private void handleNewIntent(Message msg) {
//        Object obj = msg.obj;
//        ArrayList intents = (ArrayList) RefInvoke.getFieldObject(obj, "intents");
//        Log.i(TAG,"handleNewIntent: "+ intents);
//
//        for (Object object : intents) {
//            Intent raw = (Intent) object;
//            Intent target = raw.getParcelableExtra(AMSHookHelper.EXTRA_TARGET_INTENT);
//            if (target != null) {
//                raw.setComponent(target.getComponent());
//
//                if (target.getExtras() != null) {
//                    raw.putExtras(target.getExtras());
//                }
//
//                break;
//            }
//        }
//    }

    private void handleActivity(Message msg) {
        // 这里简单起见,直接取出TargetActivity;
        Object obj = msg.obj;

        //android.app.servertransaction.ClientTransaction
        logi("handleActivity...obj: " + obj.toString());
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

                logi("handleActivity...target: " + target.toString());

                /**
                 * 其实不要也能生效。
                 */
                //修改packageName，这样缓存才能命中
                ActivityInfo activityInfo = (ActivityInfo) RefInvoke.getFieldObject(object, "mInfo");
                logi("activityInfo:" + activityInfo);
                realLaunch(target, activityInfo);
            }
        }
    }


    private void handleLaunchActivity(Message msg) {
        // 这里简单起见,直接取出TargetActivity;

        Object obj = msg.obj;
        logi("handleLaunchActivity obj:" + obj);

        // 把替身恢复成真身
        Intent raw = (Intent) RefInvoke.getFieldObject(obj, "intent");

        logi("handleLaunchActivity rawIntent:" + raw.toString());
        Intent target = raw.getParcelableExtra(AMSHookHelper.EXTRA_TARGET_INTENT);
        raw.setComponent(target.getComponent());


        //android.app.ActivityThread$ActivityClientRecord
        logi("obj: " + obj.getClass().toString());
        //修改packageName，这样缓存才能命中
        ActivityInfo activityInfo = (ActivityInfo) RefInvoke.getFieldObject(obj, "activityInfo");
        realLaunch(target, activityInfo);
    }

    private void realLaunch(Intent target, ActivityInfo activityInfo) {


        try {
            logi("===============realLaunch==============================");
            logi("realLaunch target: " + target.toString());
            logi("realLaunch activityInfo: " + activityInfo.toString());

            activityInfo.applicationInfo.packageName = target.getPackage() == null ?
                    target.getComponent().getPackageName() : target.getPackage();
            hookPackageManager();
        } catch (Throwable e) {
            logi(Log.getStackTraceString(e));

        }
    }

    private static void hookPackageManager() throws Exception {

        // 这一步是因为 initializeJavaContextClassLoader 这个方法内部无意中检查了这个包是否在系统安装
        // 如果没有安装, 直接抛出异常, 这里需要临时Hook掉 PMS, 绕过这个检查.
        Object currentActivityThread = RefInvoke.invokeStaticMethod("android.app.ActivityThread", "currentActivityThread");

        // 获取ActivityThread里面原始的 sPackageManager
        Object sPackageManager = RefInvoke.getFieldObject(currentActivityThread, "sPackageManager");

        // 准备好代理对象, 用来替换原始的对象
        Class<?> iPackageManagerInterface = Class.forName("android.content.pm.IPackageManager");
        Object proxy = Proxy.newProxyInstance(iPackageManagerInterface.getClassLoader(),
                new Class<?>[]{iPackageManagerInterface},
                new MocksPManager(sPackageManager));

        // 1. 替换掉ActivityThread里面的 sPackageManager 字段
        RefInvoke.setFieldObject(currentActivityThread, "sPackageManager", proxy);
    }

    private static final String TAG = "sanbo.MockmCallback";

    private void logd(String info) {
        Log.println(Log.DEBUG, TAG, info);
    }

    private void logi(String info) {
        Log.println(Log.INFO, TAG, info);
    }
}
