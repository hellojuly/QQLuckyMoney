package me.hellojuly.qq.luckymoney;

import android.content.ComponentName;
import android.app.Activity;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import me.hellojuly.qq.luckymoney.bean.FromServiceMsg;
import me.hellojuly.qq.luckymoney.db.ServiceMsgSQLiteHelper;

import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findFirstFieldByExactType;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.setObjectField;

/**
 * com.tencent.mobileqq.data.MessageRecord              QQ消息
 * com.tencent.mobileqq.data.MessageForQQWalletMsg      QQ红包消息
 *
 */
public class Main implements IXposedHookLoadPackage {

    private static final String QQ_PACKAGE_NAME = "com.tencent.mobileqq";
    private static final String WECHAT_PACKAGE_NAME = "com.tencent.mm";

    //MessageHandlerUtils
    static long msgUid;//消息id
    static String senderuin;//发送者uin
    static String frienduin;//朋友的uin
    static int istroop;//群
    static String selfuin;//自己的uin

    //SplashActivity
    static Context globalContext = null;
    //HotChatManager
    static Object HotChatManager = null;
    //TicketManagerImpl
    static Object TicketManager;//时钟->负责定时动态加解密的

    private void dohook(final XC_LoadPackage.LoadPackageParam loadPackageParam) {

        /**
         * 获取配置参数
         */
        findAndHookMethod("com.tencent.mobileqq.app.MessageHandlerUtils", loadPackageParam.classLoader, "a",
                "com.tencent.mobileqq.app.QQAppInterface",
                "com.tencent.mobileqq.data.MessageRecord", Boolean.TYPE, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if (!PreferencesUtils.open()) {
                            return;
                        }
                        int msgtype = (int) getObjectField(param.args[1], "msgtype");
                        if (msgtype == -2025) {//QQ红包消息MSG_TYPE_QQWALLET_MSG
                            msgUid = (long) getObjectField(param.args[1], "msgUid");
                            senderuin = (String) getObjectField(param.args[1], "senderuin");
                            frienduin = getObjectField(param.args[1], "frienduin").toString();
                            istroop = (int) getObjectField(param.args[1], "istroop");
                            selfuin = getObjectField(param.args[1], "selfuin").toString();
                        }
                    }
                }

        );

        /**
         * 解析红包消息
         */
        findAndHookMethod("com.tencent.mobileqq.data.MessageForQQWalletMsg", loadPackageParam.classLoader, "doParse", new
                XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if (!PreferencesUtils.open() || msgUid == 0) {
                            return;
                        }
                        msgUid = 0;

                        int messageType = (int) XposedHelpers.getObjectField(param.thisObject, "messageType");
                        if (messageType == 6 && !PreferencesUtils.password()) {
                            return;
                        }


                        Object mQQWalletRedPacketMsg = getObjectField(param.thisObject, "mQQWalletRedPacketMsg");
                        String redPacketId = getObjectField(mQQWalletRedPacketMsg, "redPacketId").toString();
                        String authkey = (String) getObjectField(mQQWalletRedPacketMsg, "authkey");
                        //PluginStatic.getOrCreateClassLoader(Context context, String pluginID)
                        ClassLoader walletClassLoader = (ClassLoader) callStaticMethod(findClass("com.tencent.mobileqq.pluginsdk.PluginStatic", loadPackageParam.classLoader), "getOrCreateClassLoader", globalContext, "qwallet_plugin.apk");
                        StringBuffer requestUrl = new StringBuffer();
                        requestUrl.append("&uin=" + selfuin);
                        requestUrl.append("&listid=" + redPacketId);
                        requestUrl.append("&name=" + Uri.encode(""));
                        requestUrl.append("&answer=");
                        if (istroop == 0) {
                            requestUrl.append("&groupid=" + selfuin);
                        } else {
                            requestUrl.append("&groupid=" + frienduin);
                        }
                        requestUrl.append("&grouptype=" + getGroupType());
                        requestUrl.append("&groupuin=" + senderuin);
                        requestUrl.append("&authkey=" + authkey);

                        Class findClass = findClass("com.tenpay.android.qqplugin.a.p", walletClassLoader);

                        int random = Math.abs(new Random().nextInt()) % 16;
                        String reqText = (String) callStaticMethod(findClass, "a", globalContext, Integer.valueOf(random), Boolean.valueOf(false), requestUrl.toString());
                        StringBuffer hongbaoRequestUrl = new StringBuffer();
                        hongbaoRequestUrl.append("https://mqq.tenpay.com/cgi-bin/hongbao/qpay_hb_na_grap.cgi?ver=2.0&chv=3");
                        hongbaoRequestUrl.append("&req_text=" + reqText);
                        hongbaoRequestUrl.append("&random=" + random);
                        hongbaoRequestUrl.append("&skey_type=2");
                        hongbaoRequestUrl.append("&skey=" + callMethod(TicketManager, "getSkey", selfuin));

                        Object pickObject = XposedHelpers.newInstance(findClass("com.tenpay.android.qqplugin.b.d", walletClassLoader), callStaticMethod(findClass, "a", globalContext));
                        Bundle bundle = (Bundle) callMethod(pickObject, "a", hongbaoRequestUrl.toString());
                        String pickKey = (String) callStaticMethod(findClass, "a", globalContext, Integer.valueOf(random), callStaticMethod(findClass, "a", globalContext, bundle, new JSONObject()));

                        if (PreferencesUtils.delay()) {
                            Thread.sleep(PreferencesUtils.delayTime());
                        }
                        callStaticMethod(findClass, "a", Integer.valueOf(random), pickKey);
                    }
                }

        );

        /**
         * 开屏页
         */
        findAndHookMethod("com.tencent.mobileqq.activity.SplashActivity", loadPackageParam.classLoader, "doOnCreate", Bundle.class, new

                XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        globalContext = (Context) param.thisObject;
                    }
                }

        );


        findAndHookConstructor("mqq.app.TicketManagerImpl", loadPackageParam.classLoader, "mqq.app.AppRuntime", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                TicketManager = param.thisObject;
            }
        });


        findAndHookConstructor("com.tencent.mobileqq.app.HotChatManager", loadPackageParam.classLoader, "com.tencent.mobileqq.app.QQAppInterface", new

                XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        HotChatManager = param.thisObject;
                    }
                }

        );

        findAndHookMethod("com.tencent.mobileqq.pluginsdk.PluginProxyActivity", loadPackageParam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Intent intent = (Intent) callMethod(param.thisObject, "getIntent");
                        //PluginStatic.a(Context context, String pluginLocation, String pluginPath);
                        ClassLoader classLoader = (ClassLoader) callStaticMethod(findClass("com.tencent.mobileqq.pluginsdk.PluginStatic", loadPackageParam.classLoader), "a", param.thisObject, getObjectField(param.thisObject, "k").toString(), getObjectField(param.thisObject, "i").toString());
                        //动态代理插件加载的是抢红包界面
                        if (intent.getStringExtra("pluginsdk_launchActivity").equals("com.tenpay.android.qqplugin.activity.GrapHbActivity")) {
                            findAndHookMethod("com.tenpay.android.qqplugin.activity.GrapHbActivity", classLoader, "a", JSONObject.class,
                                    new XC_MethodHook() {
                                        @Override
                                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                            Object obj = getObjectField(param.thisObject, "mCloseBtn");
                                            callMethod(param.thisObject, "finish");
                                            callMethod(obj, "performClick");
                                        }
                                    });
                        }
                    }
                }

        );

        //设置红包为已领取
        findAndHookMethod("com.tencent.mobileqq.activity.aio.item.QQWalletMsgItemBuilder", loadPackageParam.classLoader, "a", "mjv", "com.tencent.mobileqq.data.MessageForQQWalletMsg", "com.tencent.mobileqq.activity.aio.OnLongClickAndTouchListener",
                new XC_MethodHook() {
                    int issend;

                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        issend = (int) getObjectField(param.args[1], "issend");
                        if (issend != 1) {
                            setObjectField(param.args[1], "issend", 1);

                        }
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        setObjectField(param.args[1], "issend", issend);
                    }
                }

        );
    }


    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {

        if (loadPackageParam.packageName.equals(QQ_PACKAGE_NAME)) {
            XposedBridge.log("packageName=" + loadPackageParam.packageName);
            hideModule(loadPackageParam);

            int ver = Build.VERSION.SDK_INT;
            if (ver < 21) {
                findAndHookMethod("com.tencent.common.app.BaseApplicationImpl", loadPackageParam.classLoader, "onCreate", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        dohook(loadPackageParam);
                        hookQQSend(loadPackageParam);
                    }
                });
            } else {
                dohook(loadPackageParam);
                hookQQSend(loadPackageParam);
            }
        }


        if (loadPackageParam.packageName.equals(WECHAT_PACKAGE_NAME)) {
            findAndHookMethod("com.tencent.mm.ui.LauncherUI", loadPackageParam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Activity activity = (Activity) param.thisObject;
                    if (activity != null) {
                        Intent intent = activity.getIntent();
                        if (intent != null) {
                            String className = intent.getComponent().getClassName();
                            if (!TextUtils.isEmpty(className) && className.equals("com.tencent.mm.ui.LauncherUI") && intent.hasExtra("donate")) {
                                Intent donateIntent = new Intent();
                                donateIntent.setClassName(activity, "com.tencent.mm.plugin.remittance.ui.RemittanceUI");
                                donateIntent.putExtra("scene", 1);
                                donateIntent.putExtra("pay_scene", 32);
                                donateIntent.putExtra("scan_remittance_id", "011259012001125901201468688368254");
                                donateIntent.putExtra("fee", 10.0d);
                                donateIntent.putExtra("pay_channel", 12);
                                donateIntent.putExtra("receiver_name", "yang_xiongwei");
                                donateIntent.removeExtra("donate");
                                activity.startActivity(donateIntent);
                                activity.finish();
                            }
                        }
                    }
                }
            });
        }

    }

    /**
     * 将此APP从列表中隐藏
     * @param loadPackageParam
     */
    private void hideModule(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        findAndHookMethod("android.app.ApplicationPackageManager", loadPackageParam.classLoader, "getInstalledApplications", int.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                List<ApplicationInfo> applicationList = (List) param.getResult();
                List<ApplicationInfo> resultapplicationList = new ArrayList<>();
                for (ApplicationInfo applicationInfo : applicationList) {
                    String packageName = applicationInfo.packageName;
                    if (isTarget(packageName)) {
                        log("Hid package: " + packageName);
                    } else {
                        resultapplicationList.add(applicationInfo);
                    }
                }
                param.setResult(resultapplicationList);
            }
        });
        findAndHookMethod("android.app.ApplicationPackageManager", loadPackageParam.classLoader, "getInstalledPackages", int.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                List<PackageInfo> packageInfoList = (List) param.getResult();
                List<PackageInfo> resultpackageInfoList = new ArrayList<>();

                for (PackageInfo packageInfo : packageInfoList) {
                    String packageName = packageInfo.packageName;
                    if (isTarget(packageName)) {
                        log("Hid package: " + packageName);
                    } else {
                        resultpackageInfoList.add(packageInfo);
                    }
                }
                param.setResult(resultpackageInfoList);
            }
        });
        findAndHookMethod("android.app.ApplicationPackageManager", loadPackageParam.classLoader, "getPackageInfo", String.class, int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                String packageName = (String) param.args[0];
                if (isTarget(packageName)) {
                    param.args[0] = QQ_PACKAGE_NAME;
                    log("Fake package: " + packageName + " as " + QQ_PACKAGE_NAME);
                }
            }
        });
        findAndHookMethod("android.app.ApplicationPackageManager", loadPackageParam.classLoader, "getApplicationInfo", String.class, int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                String packageName = (String) param.args[0];
                if (isTarget(packageName)) {
                    param.args[0] = QQ_PACKAGE_NAME;
                    log("Fake package: " + packageName + " as " + QQ_PACKAGE_NAME);
                }
            }
        });
        findAndHookMethod("android.app.ActivityManager", loadPackageParam.classLoader, "getRunningServices", int.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                List<RunningServiceInfo> serviceInfoList = (List) param.getResult();
                List<RunningServiceInfo> resultList = new ArrayList<>();

                for (RunningServiceInfo runningServiceInfo : serviceInfoList) {
                    String serviceName = runningServiceInfo.process;
                    if (isTarget(serviceName)) {
                        log("Hid service: " + serviceName);
                    } else {
                        resultList.add(runningServiceInfo);
                    }
                }
                param.setResult(resultList);
            }
        });
        findAndHookMethod("android.app.ActivityManager", loadPackageParam.classLoader, "getRunningTasks", int.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                List<RunningTaskInfo> serviceInfoList = (List) param.getResult();
                List<RunningTaskInfo> resultList = new ArrayList<>();

                for (RunningTaskInfo runningTaskInfo : serviceInfoList) {
                    String taskName = runningTaskInfo.baseActivity.flattenToString();
                    if (isTarget(taskName)) {
                        log("Hid task: " + taskName);
                    } else {
                        resultList.add(runningTaskInfo);
                    }
                }
                param.setResult(resultList);
            }
        });
        findAndHookMethod("android.app.ActivityManager", loadPackageParam.classLoader, "getRunningAppProcesses", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                List<RunningAppProcessInfo> runningAppProcessInfos = (List) param.getResult();
                List<RunningAppProcessInfo> resultList = new ArrayList<>();

                for (RunningAppProcessInfo runningAppProcessInfo : runningAppProcessInfos) {
                    String processName = runningAppProcessInfo.processName;
                    if (isTarget(processName)) {
                        log("Hid process: " + processName);
                    } else {
                        resultList.add(runningAppProcessInfo);
                    }
                }
                param.setResult(resultList);
            }
        });
    }

    private boolean isTarget(String name) {
        return name.contains("hellojuly") || name.contains("xposed");
    }


    private int getGroupType() throws IllegalAccessException {
        int grouptype = 0;
        if (istroop == 3000) {
            grouptype = 2;

        } else if (istroop == 1) {
            Map map = (Map) findFirstFieldByExactType(HotChatManager.getClass(), Map.class).get(HotChatManager);
            if (map != null & map.containsKey(frienduin)) {
                grouptype = 5;
            } else {
                grouptype = 1;
            }
        } else if (istroop == 0) {
            grouptype = 0;
        } else if (istroop == 1004) {
            grouptype = 4;

        } else if (istroop == 1000) {
            grouptype = 3;

        } else if (istroop == 1001) {
            grouptype = 6;
        }
        return grouptype;
    }


    //hook发送消息方法
    public void hookQQSend(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        //发送消息intent
        findAndHookMethod("mqq.app.MSFServlet", loadPackageParam.classLoader, "service", "android.content.Intent", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Intent intent = (Intent) param.args[0];
                XposedBridge.log("------------------------------------------------------------------------------------");
                XposedBridge.log("type=" + intent.getType());
                XposedBridge.log("action=" + intent.getAction());
                XposedBridge.log("dataString=" + intent.getDataString());
                XposedBridge.log("scheme=" + intent.getScheme());
                XposedBridge.log("package=" + intent.getPackage());


                /**
                 * TempServlet 普通消息
                 * QZoneFeedsServlet QZone消息
                 * QZoneNotifyServlet
                 *
                 *
                 */
                try {


                    ComponentName componentName = intent.getComponent();
                    XposedBridge.log("component=" + (componentName == null ? "null" : intent.getComponent().toString()));
                    Uri uri = intent.getData();
                    XposedBridge.log("dataUri=" + (uri == null ? "null" : uri.toString()));
                    Bundle bundle = intent.getExtras();
                    XposedBridge.log("extras=" + (bundle == null ? "null" : bundle.toString()));

                    Parcelable parcelable = intent.getParcelableExtra("ToServiceMsg");
                    if (parcelable != null)
                        XposedBridge.log("ToServiceMsg=" + parcelable.toString());
                    XposedBridge.log("------------------------------------------------------------------------------------");
                } catch (Exception e) {
                    XposedBridge.log("-Exception-----------------------------------------------------------------------------------");
                }
            }
        });


//
//        findAndHookMethod("com.tencent.mobileqq.app.MessageHandler", loadPackageParam.classLoader, "a",
//                "com.tencent.qphone.base.remote.ToServiceMsg",
//                "com.tencent.qphone.base.remote.FromServiceMsg",
//                "java.lang.Object", new XC_MethodHook() {
//                    @Override
//                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//
//                        XposedBridge.log("------------------------------------------------------------------------------------onReceive1");
//                        Object fromServiceMsg = param.args[1];
//                        byte fromVersion = (byte) getObjectField(fromServiceMsg, "fromVersion");
//                        int appId = (int) getObjectField(fromServiceMsg, "appId");
//                        int appSeq = (int) getObjectField(fromServiceMsg, "appSeq");
//                        int flag = (int) getObjectField(fromServiceMsg, "flag");
//                        int resultCode = (int) getObjectField(fromServiceMsg, "resultCode");
//                        int ssoSeq = (int) getObjectField(fromServiceMsg, "ssoSeq");
//                        String errorMsg = (String) getObjectField(fromServiceMsg, "errorMsg");
//                        String serviceCmd = (String) getObjectField(fromServiceMsg, "serviceCmd");
//                        String uin = (String) getObjectField(fromServiceMsg, "uin");
//                        byte[] msgCookie = (byte[]) getObjectField(fromServiceMsg, "msgCookie");
//                        byte[] wupBufer = (byte[]) getObjectField(fromServiceMsg, "wupBuffer");
//                        HashMap attributes = (HashMap) getObjectField(fromServiceMsg, "attributes");
//                        Bundle extraData = (Bundle) getObjectField(fromServiceMsg, "extraData");
//                        Object msfCommand = getObjectField(fromServiceMsg, "msfCommand");//指令枚举类
//                        XposedBridge.log("------------------------------------------------------------------------------------onReceive2");
//
//                        //数据源
//                        FromServiceMsg msg = new FromServiceMsg();
//                        msg.fromVersion = fromVersion;
//                        msg.appId = appId;
//                        msg.appSeq = appSeq;
//                        msg.flag = flag;
//                        msg.resultCode = resultCode;
//                        msg.ssoSeq = ssoSeq;
//                        msg.errorMsg = errorMsg;
//                        msg.serviceCmd = serviceCmd;
//                        msg.uin = uin;
//                        msg.msgCookie = msgCookie;
//                        msg.wupBuffer = wupBufer;
//                        msg.attributes = attributes;
//                        msg.extraData = extraData;
//                        msg.msfCommand = msfCommand.toString();
//
//                        XposedBridge.log("------------------------------------------------------------------------------------onReceive3");
//                        new SQLiteHelperAsync().execute(msg);
//                        XposedBridge.log("------------------------------------------------------------------------------------onReceive4");
//                    }
//                });
//
//
//        findAndHookMethod("mqq.app.MSFServlet", loadPackageParam.classLoader, "onReceive", "com.tencent.qphone.base.remote.FromServiceMsg", new XC_MethodHook() {
//            @Override
//            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//
//                XposedBridge.log("------------------------------------------------------------------------------------onReceive1");
//                Object fromServiceMsg = param.args[0];
//                byte fromVersion = (byte) getObjectField(fromServiceMsg, "fromVersion");
//                int appId = (int) getObjectField(fromServiceMsg, "appId");
//                int appSeq = (int) getObjectField(fromServiceMsg, "appSeq");
//                int flag = (int) getObjectField(fromServiceMsg, "flag");
//                int resultCode = (int) getObjectField(fromServiceMsg, "resultCode");
//                int ssoSeq = (int) getObjectField(fromServiceMsg, "ssoSeq");
//                String errorMsg = (String) getObjectField(fromServiceMsg, "errorMsg");
//                String serviceCmd = (String) getObjectField(fromServiceMsg, "serviceCmd");
//                String uin = (String) getObjectField(fromServiceMsg, "uin");
//                byte[] msgCookie = (byte[]) getObjectField(fromServiceMsg, "msgCookie");
//                byte[] wupBufer = (byte[]) getObjectField(fromServiceMsg, "wupBuffer");
//                HashMap attributes = (HashMap) getObjectField(fromServiceMsg, "attributes");
//                Bundle extraData = (Bundle) getObjectField(fromServiceMsg, "extraData");
//                Object msfCommand = getObjectField(fromServiceMsg, "msfCommand");//指令枚举类
//                XposedBridge.log("------------------------------------------------------------------------------------onReceive2");
//
//                //数据源
//                FromServiceMsg msg = new FromServiceMsg();
//                msg.fromVersion = fromVersion;
//                msg.appId = appId;
//                msg.appSeq = appSeq;
//                msg.flag = flag;
//                msg.resultCode = resultCode;
//                msg.ssoSeq = ssoSeq;
//                msg.errorMsg = errorMsg;
//                msg.serviceCmd = serviceCmd;
//                msg.uin = uin;
//                msg.msgCookie = msgCookie;
//                msg.wupBuffer = wupBufer;
//                msg.attributes = attributes;
//                msg.extraData = extraData;
//                msg.msfCommand = msfCommand.toString();
//
//                XposedBridge.log("------------------------------------------------------------------------------------onReceive3");
//                try {
//                    new SQLiteHelperAsync().execute(msg);
//                } catch (Exception e) {
//                    XposedBridge.log("------------------------------------------------------------------------------------onReceive failed");
//                }
//                XposedBridge.log("------------------------------------------------------------------------------------onReceive4");
//            }
//        });
    }

    public class SQLiteHelperAsync extends AsyncTask<FromServiceMsg, Void, Void> {

        private ServiceMsgSQLiteHelper mServiceMsgSQLiteHelper;

        public SQLiteHelperAsync() {
            if (mServiceMsgSQLiteHelper == null) {
                mServiceMsgSQLiteHelper = new ServiceMsgSQLiteHelper(MyApplication.applicationContext);
            }
        }

        @Override
        protected Void doInBackground(FromServiceMsg... params) {
            mServiceMsgSQLiteHelper.insert(params[0]);
            XposedBridge.log("-----------------------------------------------------------------------------------onReceive4-");
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            XposedBridge.log("------------------------------------------------------------------------------------onReceive5");
            mServiceMsgSQLiteHelper.close();
        }
    }

}