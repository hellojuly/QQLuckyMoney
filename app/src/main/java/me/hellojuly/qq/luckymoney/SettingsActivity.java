package me.hellojuly.qq.luckymoney;


import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import me.hellojuly.qq.luckymoney.activity.ServiceMsgActivity;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

    private SettingsFragment mSettingsFragment;


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
                startActivity(new Intent(this, ServiceMsgActivity.class));
                break;
            case R.id.btn_toServiceMsg:
                break;
        }
    }


    /**
     * A placeholder fragment containing a settings view.
     */
    public static class SettingsFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getPreferenceManager().setSharedPreferencesMode(MODE_WORLD_READABLE);
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
                    Bitmap payImage = BitmapFactory.decodeResource(getResources(), R.drawable.wechat_pay);

                    Dialog builder = new Dialog(getActivity());
                    builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    builder.getWindow().setBackgroundDrawable(
                            new ColorDrawable(android.graphics.Color.TRANSPARENT));
                    ImageView imageView = new ImageView(getActivity());
                    imageView.setImageBitmap(payImage);
                    builder.addContentView(imageView, new RelativeLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT));
                    builder.show();
                    ;
                    return true;
                }
            });

        }
    }
}
