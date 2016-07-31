package me.hellojuly.qq.luckymoney;

import android.app.Application;
import android.content.Context;

import com.facebook.stetho.Stetho;

/**
 * Created by July on 2016/7/31.
 */
public class MyApplication extends Application {

    public static Context applicationContext;

    @Override
    public void onCreate() {
        super.onCreate();
        applicationContext = getApplicationContext();
        Stetho.initializeWithDefaults(this);
    }
}
