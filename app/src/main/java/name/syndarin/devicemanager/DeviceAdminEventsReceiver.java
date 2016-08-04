package name.syndarin.devicemanager;

import android.app.AlertDialog;
import android.app.admin.DeviceAdminReceiver;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;

/**
 * Created by syndarin on 8/1/16.
 */
public class DeviceAdminEventsReceiver extends DeviceAdminReceiver {

    private final static String tag = DeviceAdminEventsReceiver.class.getSimpleName();

    @Override
    public void onEnabled(Context context, Intent intent) {
        super.onEnabled(context, intent);
        Log.d(tag, "on enabled");
    }

    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
        Log.d(tag, "on disabled request");
        return "Here we warn user about risks";
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        super.onDisabled(context, intent);
        Log.d(tag, "on disabled");
    }
}
