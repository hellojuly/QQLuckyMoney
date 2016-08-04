package me.hellojuly.qq.luckymoney;


import android.Manifest;
import android.annotation.TargetApi;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import de.robv.android.xposed.XposedHelpers;
import me.hellojuly.qq.luckymoney.activity.MessageReceiverActivity;
import me.hellojuly.qq.luckymoney.activity.MessageSenderActivity;
import me.hellojuly.qq.luckymoney.activity.MessageSenderTableActivity;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

    private SettingsFragment mSettingsFragment;
    private EditText et_troopNumber;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        if (savedInstanceState == null) {
            mSettingsFragment = new SettingsFragment();
            replaceFragment(R.id.settings_container, mSettingsFragment);
        }

        findViewById(R.id.btn_fromServiceMsg).setOnClickListener(this);
        findViewById(R.id.btn_toServiceMsg).setOnClickListener(this);
        findViewById(R.id.btn_joinTroop).setOnClickListener(this);
        findViewById(R.id.btn_senderTable).setOnClickListener(this);
        et_troopNumber = (EditText) findViewById(R.id.et_troopNumber);

        checkPermission();
    }

    private static final int REQUEST_CODE_PERMISSION_GRANTED = 1;//访问外部存储

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            int checkStorageExtraWritePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (checkStorageExtraWritePermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_PERMISSION_GRANTED);
                return;
            } else {
            }
        } else {
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_PERMISSION_GRANTED:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    // Permission Denied
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void replaceFragment(int viewId, android.app.Fragment fragment) {
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(viewId, fragment).commit();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_fromServiceMsg:
                startActivity(new Intent(this, MessageReceiverActivity.class));
                break;
            case R.id.btn_toServiceMsg:
                startActivity(new Intent(this, MessageSenderActivity.class));
                break;
            case R.id.btn_joinTroop:
                joinTroop();
                break;
            case R.id.btn_senderTable:
                startActivity(new Intent(this, MessageSenderTableActivity.class));
                break;
        }
    }

    private void joinTroop() {
        int optStat = 31;
        String content = "我是xxx";
        String troopNumber = et_troopNumber.getText().toString().trim();
        joinTroopXposed(0, null, content, troopNumber, optStat, null);
    }

    private void joinTroopXposed(int k_i, String k_str, String content, String troopNumber, int optStat, String picUrl) {
    }

    /**
     * A placeholder fragment containing a settings view.
     */
    public static class SettingsFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_setting);

            Preference donateAlipay = findPreference("donate_alipay");
            donateAlipay.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference pref) {
                    Intent intent = new Intent();
                    intent.setAction("android.intent.action.VIEW");
                    String payUrl = "https://qr.alipay.com/apbvye346u4wqkcr9b";
                    intent.setData(Uri.parse("alipayqr://platformapi/startapp?saId=10000007&clientVersion=3.7.0.0718&qrcode=" + payUrl));
                    if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                        startActivity(intent);
                    } else {
                        intent.setData(Uri.parse(payUrl));
                        startActivity(intent);
                    }
                    return true;
                }
            });

            Preference donateWechat = findPreference("donate_wechat");

            donateWechat.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference pref) {

                    Intent intent = new Intent();
                    intent.setClassName("com.tencent.mm", "com.tencent.mm.ui.LauncherUI");
                    intent.putExtra("donate", true);
                    startActivity(intent);

                    return true;
                }
            });

        }
    }
}
