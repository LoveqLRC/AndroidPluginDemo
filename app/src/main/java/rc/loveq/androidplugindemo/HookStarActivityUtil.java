package rc.loveq.androidplugindemo;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Author：Rc
 * 0n 2018/2/10 19:36
 */

public class HookStarActivityUtil {
    private Context mContext;
    private Class<?> mProxyClass;
    private final String EXTRA_ORIGIN_INTENT = "EXTRA_ORIGIN_INTENT";

    public HookStarActivityUtil(Context context, Class<?> proxyClass) {
        mContext = context;
        mProxyClass = proxyClass;
    }

    public void hookStartActivity() throws Exception {
        Class<?> amnClass = Class.forName("android.app.ActivityManagerNative");
        Field gDefaultField = amnClass.getDeclaredField("gDefault");
        gDefaultField.setAccessible(true);
        Object gDefault = gDefaultField.get(null);

        Class<?> singletonClass = Class.forName("android.util.Singleton");
        Field mInstanceField = singletonClass.getDeclaredField("mInstance");
        mInstanceField.setAccessible(true);
        Object iamInstance = mInstanceField.get(gDefault);


        Class<?> iamClass = Class.forName("android.app.IActivityManager");
        iamInstance = Proxy.newProxyInstance(
                HookStarActivityUtil.class.getClassLoader(),
                new Class[]{iamClass},
                new StartActivityInvocationHandler(iamInstance)
        );
        mInstanceField.set(gDefault, iamInstance);


    }

    public void hookLaunchActivity() throws Exception {
        Class<?> atClass = Class.forName("android.app.ActivityThread");
        Field scatField = atClass.getDeclaredField("sCurrentActivityThread");
        scatField.setAccessible(true);
        Object sCurrentActivityThread = scatField.get(null);
        //获取ActivityThread中的mH
        Field mHField = atClass.getDeclaredField("mH");
        mHField.setAccessible(true);
        Object mHandler = mHField.get(sCurrentActivityThread);

        Class<?> handlerClass = Class.forName("android.os.Handler");
        Field callbackField = handlerClass.getDeclaredField("mCallback");
        callbackField.setAccessible(true);
        callbackField.set(mHandler, new HandlerCallBack());
    }

    private class HandlerCallBack implements Handler.Callback {

        @Override
        public boolean handleMessage(Message msg) {
            Log.d("HandlerCallBack", "handleMessage");
            if (msg.what == 100) {
                handleLaunchActivity(msg);
            }
            return false;
        }
    }

    private void handleLaunchActivity(Message msg) {
        try {
            Object activityClientRecord = msg.obj;
            Field intentField = activityClientRecord.getClass().getDeclaredField("intent");
            intentField.setAccessible(true);
            Intent proxyIntent = (Intent) intentField.get(activityClientRecord);
            Intent originIntent = proxyIntent.getParcelableExtra(EXTRA_ORIGIN_INTENT);
            if (originIntent != null) {
                intentField.set(activityClientRecord, originIntent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class StartActivityInvocationHandler implements InvocationHandler {
        //方法执行者
        private Object mObject;

        public StartActivityInvocationHandler(Object object) {
            mObject = object;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Log.e("StartActivityInvocation", method.getName());
            //狸猫换太子
            if (method.getName().equals("startActivity")) {
                //首先获取原来的Intent
                Intent originIntent = (Intent) args[2];
                //创建代理的Intent
                Intent proxyIntent = new Intent(mContext, mProxyClass);
                //绑定原来的Intent
                proxyIntent.putExtra(EXTRA_ORIGIN_INTENT, originIntent);
                //替换原来的Intent
                args[2] = proxyIntent;
            }
            return method.invoke(mObject, args);
        }
    }
}
