package name.syndarin.devicemanager;

import android.Manifest;
import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityWindowInfo;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by vtiahotenkov on 8/4/16.
 */
public class WatchdogAccessibilityService extends AccessibilityService {

    private static final String TAG = WatchdogAccessibilityService.class.getSimpleName();

    private List<String> mLabelsOfProtectedResources;

    private List<String> mAppsWithInternetPermission;

    @Override
    public void onCreate() {
        super.onCreate();
        mLabelsOfProtectedResources = new ArrayList<>();
        mLabelsOfProtectedResources.add(getString(R.string.accessibility_service_label));
        mLabelsOfProtectedResources.add(getString(R.string.receiver_label));


        mAppsWithInternetPermission = new ArrayList<>();

        PackageManager pm = getPackageManager();
        String[] dangerousPermission = new String[]{Manifest.permission.INTERNET};
        List<PackageInfo> packages = pm.getPackagesHoldingPermissions(dangerousPermission, PackageManager.GET_PERMISSIONS);

        for (PackageInfo pi : packages) {
            mAppsWithInternetPermission.add(pi.packageName);
        }

        mAppsWithInternetPermission.remove("com.android.settings");
        mAppsWithInternetPermission.remove(getLauncherPackageName());
    }

    private String getLauncherPackageName(){
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        ResolveInfo resolveInfo = getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        String currentHomePackage = resolveInfo.activityInfo.packageName;
        Log.i("tag", "Launcher package name detected - " + currentHomePackage);
        return currentHomePackage;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        if(!ChildProtectionDemoApplication.getsInstance().isProtectionEnabled()) {
            Log.d(TAG, "Protection is disabled, ignore event");
            return;
        }

        if (accessibilityEvent.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            List<CharSequence> texts = accessibilityEvent.getText();
            for (CharSequence cs : texts) {
                String label = cs.toString();
                if (mLabelsOfProtectedResources.contains(label)) {
                    closeCurrentWindow();
                    break;
                }
            }
        }

        if (accessibilityEvent.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED && !ChildProtectionDemoApplication.getsInstance().isVpnEnabled()) {

            String packageName = accessibilityEvent.getPackageName().toString();

            if (mAppsWithInternetPermission.contains(packageName)) {
                closeCurrentWindow();
            }
        }
    }



    private void closeCurrentWindow() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                performGlobalAction(GLOBAL_ACTION_BACK);
                startApplication();
            }
        }, 200);
    }

    private void startApplication(){
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onInterrupt() {

    }

}
