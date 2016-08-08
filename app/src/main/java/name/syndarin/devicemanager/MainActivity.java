package name.syndarin.devicemanager;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final static int REQUEST_CODE_ENABLE_ADMIN = 900;

    private final static String TAG = MainActivity.class.getSimpleName();

    private DevicePolicyManager mDevicePolicyManager;
    private ComponentName mDeviceAdminReceiver;

    private Button mButtonManageAdminSettings;
    private Button mButtonEnableAccessibilitySettings;
    private Button mButtonDisableParentControl;

    private EditText mEditTextPassword;

    private View mLayoutDisableControl;
    private View mLayoutSetupControl;

    private TextView mTextViewAdminHints;
    private TextView mTextViewAccessoryHints;
    private TextView mTextViewVpnDescription;

    private CheckBox mCheckBoxVpn;

    private BroadcastReceiver mAdminStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            invalidateUi();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextViewVpnDescription = (TextView) findViewById(R.id.text_vpn_description);

        mEditTextPassword = (EditText) findViewById(R.id.edit_password);

        mTextViewAccessoryHints = (TextView) findViewById(R.id.text_accessory_hints);
        mTextViewAdminHints = (TextView) findViewById(R.id.text_admin_hints);

        mButtonDisableParentControl = (Button) findViewById(R.id.button_disable_control);
        mButtonDisableParentControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                disableParentControl();
            }
        });

        mLayoutDisableControl = findViewById(R.id.layout_unlock);
        mLayoutSetupControl = findViewById(R.id.layout_setup_control);

        mButtonEnableAccessibilitySettings = (Button) findViewById(R.id.button_enable_accessibility_settings);
        mButtonEnableAccessibilitySettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAccessibilityServiceSettings();
            }
        });

        mButtonManageAdminSettings = (Button) findViewById(R.id.button_enable_admin);
        mButtonManageAdminSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestAdmin();
            }
        });

        mCheckBoxVpn = (CheckBox) findViewById(R.id.cb_use_vpn);
        mCheckBoxVpn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                ChildProtectionDemoApplication.getsInstance().setVpnEnabled(b);
                mTextViewVpnDescription.setText(b ? R.string.description_vpn_enabled : R.string.description_vpn_enabled);
            }
        });

        mDevicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        mDeviceAdminReceiver = new ComponentName(this, DeviceAdminEventsReceiver.class);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mAdminStatusReceiver, new IntentFilter(DeviceAdminEventsReceiver.ADMIN_STATE_CHANGED));
        invalidateUi();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mAdminStatusReceiver);
    }

    private void invalidateUi(){
        boolean isAdminEnabled = mDevicePolicyManager.isAdminActive(mDeviceAdminReceiver);
        Log.d(TAG, "is admin enabled - " + isAdminEnabled);
        mButtonManageAdminSettings.setEnabled(!isAdminEnabled);
        mTextViewAdminHints.setText(isAdminEnabled ? R.string.description_admin_mode_enabled : R.string.description_admin_mode_disabled);

        boolean isAccessibilityEnabled = isAccessibilityServiceEnabled();
        mButtonEnableAccessibilitySettings.setEnabled(!isAccessibilityEnabled);
        mTextViewAccessoryHints.setText(isAccessibilityEnabled ? R.string.description_accessibility_service_enabled : R.string.description_accessibility_service_disabled);

        boolean isParentControlEnabled = isAdminEnabled && isAccessibilityEnabled;

        ChildProtectionDemoApplication.getsInstance().setProtectionEnabled(isParentControlEnabled);

        if (isParentControlEnabled) {
            mLayoutDisableControl.setVisibility(View.VISIBLE);
            mLayoutSetupControl.setVisibility(View.GONE);
        } else {
            mLayoutDisableControl.setVisibility(View.GONE);
            mLayoutSetupControl.setVisibility(View.VISIBLE);
        }

        Toast.makeText(this, isParentControlEnabled ? "Parent control enabled" : "Parent control disabled" , Toast.LENGTH_SHORT).show();
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

    private boolean isAccessibilityServiceEnabled(){
        AccessibilityManager am = (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityEvent.TYPES_ALL_MASK);
        Log.d(TAG, "Enabled count - " + enabledServices.size());
        for (AccessibilityServiceInfo info : enabledServices) {
            Log.d(TAG, info.getSettingsActivityName());
            if (info.getSettingsActivityName().equalsIgnoreCase("name.syndarin.devicemanager.MainActivity")){
                return true;
            }
        }
        return false;
    }

    private void openAccessibilityServiceSettings(){
        Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        startActivityForResult(intent, 0);
    }

    private void disableParentControl(){
        String password = mEditTextPassword.getText().toString();
        if (password.equals("1111")) {
            removeAdmin();
        } else {
            Toast.makeText(this, "Incorrect password!", Toast.LENGTH_SHORT).show();
        }
    }


}
