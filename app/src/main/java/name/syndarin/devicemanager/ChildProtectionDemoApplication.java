package name.syndarin.devicemanager;

import android.app.Application;

/**
 * Created by syndarin on 8/4/16.
 */
public class ChildProtectionDemoApplication extends Application {

    private static ChildProtectionDemoApplication sInstance;

    public static ChildProtectionDemoApplication getsInstance() {
        return sInstance;
    }

    private boolean mProtectionEnabled;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
    }

    public boolean isProtectionEnabled() {
        return mProtectionEnabled;
    }

    public void setProtectionEnabled(boolean protectionEnabled) {
        mProtectionEnabled = protectionEnabled;
    }
}
