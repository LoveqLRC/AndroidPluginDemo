package rc.loveq.androidplugindemo;

import android.app.Application;

/**
 * Author：Rc
 * 0n 2018/2/10 20:11
 */

public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        HookStarActivityUtil hookStarActivityUtil =
                new HookStarActivityUtil(this, ProxyActivity.class);
        try {
            hookStarActivityUtil.hookStartActivity();
            hookStarActivityUtil.hookLaunchActivity();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
