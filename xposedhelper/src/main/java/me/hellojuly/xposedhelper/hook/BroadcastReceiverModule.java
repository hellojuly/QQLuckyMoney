package me.hellojuly.xposedhelper.hook;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

/**
 * Desc: 注册广播
 * Created by 庞承晖
 * Date: 2016/8/4.
 * Time: 17:37
 */
public class BroadcastReceiverModule {

    public static void registerReceiver(XC_LoadPackage.LoadPackageParam loadPackageParam, final BroadcastReceiver receiver, final String[] actions) {
        findAndHookMethod("android.app.Application", loadPackageParam.classLoader, "onCreate", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Context context = (Context) param.thisObject;
                IntentFilter filter = new IntentFilter();
                if (actions == null) return;
                for (String action : actions) {
                    filter.addAction(action);
                }
                context.registerReceiver(receiver, filter);
            }
        });
    }

}
