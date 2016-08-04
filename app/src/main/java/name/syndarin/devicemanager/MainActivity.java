package name.syndarin.devicemanager;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private final static int REQUEST_CODE_ENABLE_ADMIN = 900;

    private final static String TAG = MainActivity.class.getSimpleName();

    private DevicePolicyManager mDevicePolicyManager;

    private ComponentName mDeviceAdminReceiver;

    private Button mButtonManageAdminSettings;

    private boolean mIsAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mButtonManageAdminSettings = (Button) findViewById(R.id.button_enable_admin);
        mButtonManageAdminSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mIsAdmin) {
                    removeAdmin();
                } else {
                    requestAdmin();
                }
            }
        });

        mDevicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);

        mDeviceAdminReceiver = new ComponentName(this, DeviceAdminEventsReceiver.class);

        mIsAdmin = mDevicePolicyManager.isAdminActive(mDeviceAdminReceiver);

        updateButtonLabel();

    }

    private void updateButtonLabel(){
        mButtonManageAdminSettings.setText(mIsAdmin ? R.string.button_stop_admin : R.string.button_become_admin);
    }

    private void requestAdmin(){
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mDeviceAdminReceiver);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, getString(R.string.admin_explanation));
        startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN);
    }

    private void removeAdmin(){
        mDevicePolicyManager.removeActiveAdmin(mDeviceAdminReceiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_ENABLE_ADMIN) {
            mIsAdmin = resultCode == RESULT_OK;
            Log.d(TAG, "Admin is enabled - " + String.valueOf(mIsAdmin));
            updateButtonLabel();
        }
    }
}
