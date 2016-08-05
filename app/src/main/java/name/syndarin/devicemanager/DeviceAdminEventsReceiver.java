package name.syndarin.devicemanager;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by syndarin on 8/5/16.
 */
public class DeviceAdminEventsReceiver extends DeviceAdminReceiver {

    public final static String ADMIN_STATE_CHANGED = "name.syndarin.devicemanager.DeviceAdminEventsReceiver";

    private final String tag = DeviceAdminEventsReceiver.class.getSimpleName();

    public DeviceAdminEventsReceiver() {
    }

    @Override
    public void onEnabled(Context context, Intent intent) {
        super.onEnabled(context, intent);
        Log.d(tag, "on enabled");
        sendInvalidateBroadcast(context);
    }

    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
        Log.d(tag, "on disabled request");
        return "Here we will warn user about risks";
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        super.onDisabled(context, intent);
        Log.d(tag, "on disabled");
        sendInvalidateBroadcast(context);
    }

    private void sendInvalidateBroadcast(Context context){
        Intent i = new Intent(ADMIN_STATE_CHANGED);
        context.sendBroadcast(i);
    }

}
