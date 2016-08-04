package me.hellojuly.qq.luckymoney;

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
import android.text.TextUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import me.hellojuly.qq.luckymoney.bean.FromServiceMsg;
import me.hellojuly.qq.luckymoney.bean.MSFCmd;
import me.hellojuly.qq.luckymoney.bean.ServiceCmd;
import me.hellojuly.qq.luckymoney.bean.ToServiceMsg;
import me.hellojuly.qq.luckymoney.db.DatabaseContext;
import me.hellojuly.qq.luckymoney.db.ServiceMsgSQLiteHelper;
import me.hellojuly.xposedhelper.utils.HidePackageUtil;

import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findField;
import static de.robv.android.xposed.XposedHelpers.findFirstFieldByExactType;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.setObjectField;

/**
 * com.tencent.mobileqq.data.MessageRecord              QQ消息
 * com.tencent.mobileqq.data.MessageForQQWalletMsg      QQ红包消息
 */
public class Main implements IXposedHookLoadPackage {

    private static final String QQ_PACKAGE_NAME = "com.tencent.mobileqq";
    private static final String WECHAT_PACKAGE_NAME = "com.tencent.mm";
    private static final String SELF_PACKAGE_NAME = "me.hellojuly.qq.luckymoney";

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

    static Object QQAppInterface = null;
    Object addFriendVerifyActivity;
    Object troopHandler;

    boolean isJoin = true;

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

                        runJoinTroop();
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
            HidePackageUtil.hideModule(loadPackageParam, "hellojuly");

            int ver = Build.VERSION.SDK_INT;
            if (ver < 21) {
                findAndHookMethod("com.tencent.common.app.BaseApplicationImpl", loadPackageParam.classLoader, "onCreate", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        dohook(loadPackageParam);
                        hookQQSend(loadPackageParam);
                        hookAddFriend(loadPackageParam);
                        hookAccount(loadPackageParam);
                    }
                });
            } else {
                dohook(loadPackageParam);
                hookQQSend(loadPackageParam);
                hookAddFriend(loadPackageParam);
                hookAccount(loadPackageParam);
            }
        }

        //hook自己
        if (loadPackageParam.packageName.equals(SELF_PACKAGE_NAME)) {
            XposedBridge.log("packageName=" + loadPackageParam.packageName);
            hookSelf(loadPackageParam);
        }

//        if (loadPackageParam.packageName.equals(WECHAT_PACKAGE_NAME)) {
//            findAndHookMethod("com.tencent.mm.ui.LauncherUI", loadPackageParam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
//                @Override
//                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                    Activity activity = (Activity) param.thisObject;
//                    if (activity != null) {
//                        Intent intent = activity.getIntent();
//                        if (intent != null) {
//                            String className = intent.getComponent().getClassName();
//                            if (!TextUtils.isEmpty(className) && className.equals("com.tencent.mm.ui.LauncherUI") && intent.hasExtra("donate")) {
//                                Intent donateIntent = new Intent();
//                                donateIntent.setClassName(activity, "com.tencent.mm.plugin.remittance.ui.RemittanceUI");
//                                donateIntent.putExtra("scene", 1);
//                                donateIntent.putExtra("pay_scene", 32);
//                                donateIntent.putExtra("scan_remittance_id", "011259012001125901201468688368254");
//                                donateIntent.putExtra("fee", 10.0d);
//                                donateIntent.putExtra("pay_channel", 12);
//                                donateIntent.putExtra("receiver_name", "yang_xiongwei");
//                                donateIntent.removeExtra("donate");
//                                activity.startActivity(donateIntent);
//                                activity.finish();
//                            }
//                        }
//                    }
//                }
//            });
//        }

    }

    /**
     * hook 自己
     *
     * @param loadPackageParam
     */
    private void hookSelf(final XC_LoadPackage.LoadPackageParam loadPackageParam) {
        findAndHookMethod("me.hellojuly.qq.luckymoney.SettingsActivity", loadPackageParam.classLoader, "joinTroopXposed",
                int.class,
                String.class,
                String.class,
                String.class,
                int.class,
                String.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        int k_i = (int) param.args[0];
                        String k_str = (String) param.args[1];
                        String content = (String) param.args[2];
                        String troopNumber = (String) param.args[3];
                        int optStat = (int) param.args[4];
                        String picUrl = (String) param.args[5];
                    }
                });
    }

    private void runJoinTroop() {
        if (!isJoin) return;

        String troopNumber = "548819528";
        String content = "我是xxx";
        int optStat = 31;
        String picUrl = null;
        int k_i = 0;
        String k_str = null;


        String account = (String) callMethod(QQAppInterface, "getAccount");
        XposedBridge.log("account=" + account);
        if (TextUtils.isEmpty(account)) {
            XposedBridge.log("QQ号不能为null");
            return;
        }
        byte[] bytes = (byte[]) callMethod(addFriendVerifyActivity, "a", k_i, k_str, content, account, Long.parseLong(troopNumber));
        callMethod(troopHandler, "a", troopNumber, content, optStat, bytes, picUrl);
        isJoin = false;
    }

    /**
     * hook 账号信息
     *
     * @param loadPackageParam
     */
    private void hookAccount(final XC_LoadPackage.LoadPackageParam loadPackageParam) {
        findAndHookConstructor("com.tencent.mobileqq.app.QQAppInterface", loadPackageParam.classLoader,
                "com.tencent.common.app.BaseApplicationImpl",
                String.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log("--------------------------- Constructor QQAppInterface ---------------------------");
                        if (param.thisObject == null) {
                            XposedBridge.log("--------------------------- Constructor QQAppInterface null ---------------------------");
                        } else {
                            QQAppInterface = param.thisObject;
                            addFriendVerifyActivity = XposedHelpers.newInstance(findClass("com.tencent.mobileqq.activity.AddFriendVerifyActivity", loadPackageParam.classLoader));
                            troopHandler = XposedHelpers.newInstance(findClass("com.tencent.mobileqq.app.TroopHandler", loadPackageParam.classLoader), QQAppInterface);
                        }
                    }
                });
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

        //发送消息体ToServiceMsg
        findAndHookMethod("com.tencent.mobileqq.msf.sdk.MsfServiceSdk", loadPackageParam.classLoader, "sendMsg", "com.tencent.qphone.base.remote.ToServiceMsg", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Object toServiceMsg = param.args[0];
                byte toVersion = (byte) getObjectField(toServiceMsg, "toVersion");
                int appId = (int) getObjectField(toServiceMsg, "appId");
                int appSeq = (int) getObjectField(toServiceMsg, "appSeq");
                int ssoSeq = (int) getObjectField(toServiceMsg, "ssoSeq");
                String uin = (String) getObjectField(toServiceMsg, "uin");

                boolean needResp = (boolean) getObjectField(toServiceMsg, "needResp");
                boolean quickSendEnable = (boolean) getObjectField(toServiceMsg, "quickSendEnable");
                int quickSendStrategy = (int) getObjectField(toServiceMsg, "quickSendStrategy");
                long sendTimeout = (long) getObjectField(toServiceMsg, "sendTimeout");
                long timeout = (long) getObjectField(toServiceMsg, "timeout");
                byte uinType = (byte) getObjectField(toServiceMsg, "uinType");
                String serviceName = (String) getObjectField(toServiceMsg, "serviceName");

                String serviceCmd = (String) getObjectField(toServiceMsg, "serviceCmd");
                Object msfCommand = getObjectField(toServiceMsg, "msfCommand");//指令枚举类
                byte[] wupBuffer = (byte[]) getObjectField(toServiceMsg, "wupBuffer");
                HashMap attributes = (HashMap) getObjectField(toServiceMsg, "attributes");
                Bundle extraData = (Bundle) getObjectField(toServiceMsg, "extraData");

                ToServiceMsg msg = new ToServiceMsg();
                msg.toVersion = toVersion;
                msg.appId = appId;
                msg.appSeq = appSeq;
                msg.ssoSeq = ssoSeq;
                msg.uin = uin;

                msg.needResp = needResp;
                msg.quickSendEnable = quickSendEnable;
                msg.quickSendStrategy = quickSendStrategy;
                msg.sendTimeout = sendTimeout;
                msg.timeout = timeout;
                msg.uinType = uinType;
                msg.serviceName = serviceName;

                msg.serviceCmd = serviceCmd;
                msg.msfCommand = msfCommand.toString();
                msg.wupBuffer = wupBuffer;
                msg.attributes = attributes;
                msg.extraData = extraData == null ? "" : extraData.toString();

                try {
                    new SenderSQLiteHelperAsync().execute(msg);
                } catch (Exception e) {
                }
            }
        });

        //接收消息体FromServiceMsg
        findAndHookMethod("mqq.app.MSFServlet", loadPackageParam.classLoader, "onReceive", "com.tencent.qphone.base.remote.FromServiceMsg", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                Object fromServiceMsg = param.args[0];
                byte fromVersion = (byte) getObjectField(fromServiceMsg, "fromVersion");
                int appId = (int) getObjectField(fromServiceMsg, "appId");
                int appSeq = (int) getObjectField(fromServiceMsg, "appSeq");
                int flag = (int) getObjectField(fromServiceMsg, "flag");
                int resultCode = (int) getObjectField(fromServiceMsg, "resultCode");
                int ssoSeq = (int) getObjectField(fromServiceMsg, "ssoSeq");
                String errorMsg = (String) getObjectField(fromServiceMsg, "errorMsg");
                String serviceCmd = (String) getObjectField(fromServiceMsg, "serviceCmd");
                String uin = (String) getObjectField(fromServiceMsg, "uin");
                byte[] msgCookie = (byte[]) getObjectField(fromServiceMsg, "msgCookie");
                byte[] wupBuffer = (byte[]) getObjectField(fromServiceMsg, "wupBuffer");
                HashMap attributes = (HashMap) getObjectField(fromServiceMsg, "attributes");
                Bundle extraData = (Bundle) getObjectField(fromServiceMsg, "extraData");
                Object msfCommand = getObjectField(fromServiceMsg, "msfCommand");//指令枚举类

                //数据源
                FromServiceMsg msg = new FromServiceMsg();
                msg.fromVersion = fromVersion;
                msg.appId = appId;
                msg.appSeq = appSeq;
                msg.flag = flag;
                msg.resultCode = resultCode;
                msg.ssoSeq = ssoSeq;
                msg.errorMsg = errorMsg;
                msg.serviceCmd = serviceCmd;
                msg.uin = uin;
                msg.msgCookie = msgCookie;
                msg.wupBuffer = wupBuffer;
                msg.attributes = attributes;
                msg.extraData = extraData == null ? "" : extraData.toString();
                msg.msfCommand = msfCommand.toString();

//                XposedBridge.log(msg.toString());
                try {
                    new ReceiverSQLiteHelperAsync().execute(msg);
                } catch (Exception e) {
                }
            }
        });
    }


    private void hookAddFriend(XC_LoadPackage.LoadPackageParam loadPackageParam) {

        //TroopHandler处理加群消息
        //String , String , int , String , byte[] , String
        findAndHookMethod("com.tencent.mobileqq.app.TroopHandler", loadPackageParam.classLoader, "a",
                String.class,
                String.class,
                int.class,
                String.class,
                byte[].class,
                String.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);

                        String troopUin = (String) param.args[0];
                        String content = (String) param.args[1];
                        int optStat = (int) param.args[2];
                        String authKey = (String) param.args[3];
                        byte[] bytes = (byte[]) param.args[4];
                        String picUrl = (String) param.args[5];

                        XposedBridge.log("-TroopHandler-----------------------------------------------------------------------------");
                        XposedBridge.log("troopUin=" + troopUin);
                        XposedBridge.log("content=" + content);
                        XposedBridge.log("optStat=" + optStat);
                        XposedBridge.log("authKey=" + authKey);
                        XposedBridge.log("picUrl=" + picUrl);
                        XposedBridge.log("bytes=" + (bytes == null ? 0 : bytes.length));
                        XposedBridge.log("------------------------------------------------------------------------------");
                    }
                });

        //AddFriendVerifyActivity处理加群消息
        //int , String , String , long , long
        findAndHookMethod("com.tencent.mobileqq.activity.AddFriendVerifyActivity", loadPackageParam.classLoader, "a",
                int.class,
                String.class,
                String.class,
                long.class,
                long.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);

                        int i_k = (int) param.args[0];
                        String str_k = (String) param.args[1];
                        String content = (String) param.args[2];
                        long account = (long) param.args[3];
                        long uni = (long) param.args[4];

                        XposedBridge.log("-AddFriendVerifyActivity-----------------------------------------------------------------------------");
                        XposedBridge.log("i_k=" + i_k);
                        XposedBridge.log("str_k=" + str_k);
                        XposedBridge.log("content=" + content);
                        XposedBridge.log("account=" + account);
                        XposedBridge.log("uni=" + uni);
                        XposedBridge.log("------------------------------------------------------------------------------");
                    }
                });

        //AddFriendLogicActivity处理加群消息
        //Context context, String uin, String nickName, short groupOption, int optStat, String troopQuestion, String troopAnswer, String returnAddr, String lastActivity
        findAndHookMethod("com.tencent.mobileqq.activity.AddFriendLogicActivity", loadPackageParam.classLoader, "a",
                "android.content.Context",
                String.class,
                String.class,
                short.class,
                int.class,
                String.class,
                String.class,
                String.class,
                String.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);

                        String uin = (String) param.args[1];
                        String nickName = (String) param.args[2];
                        short groupOption = (short) param.args[3];
                        int optStat = (int) param.args[4];
                        String troopQuestion = (String) param.args[5];
                        String troopAnswer = (String) param.args[6];
                        String returnAddr = (String) param.args[7];
                        String lastActivity = (String) param.args[8];

                        XposedBridge.log("-AddFriendLogicActivity not authkey-----------------------------------------------------------------------------");
                        XposedBridge.log("uin=" + uin);
                        XposedBridge.log("nickName=" + nickName);
                        XposedBridge.log("groupOption=" + groupOption);
                        XposedBridge.log("optStat=" + optStat);
                        XposedBridge.log("troopQuestion=" + troopQuestion);
                        XposedBridge.log("troopAnswer=" + troopAnswer);
                        XposedBridge.log("returnAddr=" + returnAddr);
                        XposedBridge.log("lastActivity=" + lastActivity);
                        XposedBridge.log("------------------------------------------------------------------------------");
                    }
                });

        //AddFriendLogicActivity处理加群消息
        //Context context, String uin, String nickName, short groupOption, int optStat, String troopQuestion, String troopAnswer, String returnAddr, String lastActivity, String authKey
        findAndHookMethod("com.tencent.mobileqq.activity.AddFriendLogicActivity", loadPackageParam.classLoader, "a",
                "android.content.Context",
                String.class,
                String.class,
                short.class,
                int.class,
                String.class,
                String.class,
                String.class,
                String.class,
                String.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);

                        String uin = (String) param.args[1];
                        String nickName = (String) param.args[2];
                        short groupOption = (short) param.args[3];
                        int optStat = (int) param.args[4];
                        String troopQuestion = (String) param.args[5];
                        String troopAnswer = (String) param.args[6];
                        String returnAddr = (String) param.args[7];
                        String lastActivity = (String) param.args[8];
                        String authKey = (String) param.args[9];

                        XposedBridge.log("-AddFriendLogicActivity has authkey-----------------------------------------------------------------------------");
                        XposedBridge.log("uin=" + uin);
                        XposedBridge.log("nickName=" + nickName);
                        XposedBridge.log("groupOption=" + groupOption);
                        XposedBridge.log("optStat=" + optStat);
                        XposedBridge.log("troopQuestion=" + troopQuestion);
                        XposedBridge.log("troopAnswer=" + troopAnswer);
                        XposedBridge.log("returnAddr=" + returnAddr);
                        XposedBridge.log("lastActivity=" + lastActivity);
                        XposedBridge.log("authKey=" + authKey);
                        XposedBridge.log("------------------------------------------------------------------------------");
                    }
                });
    }

    /**
     * 添加群及添加好友的Activity
     */
    private void hookAddFriendActivity(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        //添加好友Intent
        findAndHookMethod("com.tencent.mobileqq.activity.AddFriendLogicActivity", loadPackageParam.classLoader, "a",
                "android.content.Context",
                int.class,
                String.class,
                String.class,
                int.class,
                int.class,
                String.class,
                String.class,
                String.class,
                String.class,
                String.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        int uinType = (int) param.args[1];
                        String uin = (String) param.args[2];
                        String extra = (String) param.args[3];
                        int sourceId = (int) param.args[4];
                        int subSourceId = (int) param.args[5];
                        String nickName = (String) param.args[6];
                        String msg = (String) param.args[7];
                        String returnAddr = (String) param.args[8];
                        String lastActivity = (String) param.args[9];
                        String srcName = (String) param.args[10];

                        XposedBridge.log("-Add Friend---------------------------------------------------------------------------");
                        XposedBridge.log("uinType=" + uinType);
                        XposedBridge.log("uin=" + uin);
                        XposedBridge.log("extra=" + extra);
                        XposedBridge.log("sourceId=" + sourceId);
                        XposedBridge.log("subSourceId=" + subSourceId);
                        XposedBridge.log("nickName=" + nickName);
                        XposedBridge.log("msg=" + msg);
                        XposedBridge.log("returnAddr=" + returnAddr);
                        XposedBridge.log("lastActivity=" + lastActivity);
                        XposedBridge.log("lastActivity=" + srcName);
                        XposedBridge.log("--------------------------------------------------------------------------------------");
                    }
                });

        //添加群Intent
        findAndHookMethod("com.tencent.mobileqq.activity.AddFriendLogicActivity", loadPackageParam.classLoader, "a",
                "android.content.Context",
                String.class,
                String.class,
                short.class,
                int.class,
                String.class,
                String.class,
                String.class,
                String.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        String uin = (String) param.args[1];
                        String nickName = (String) param.args[2];
                        short groupOption = (short) param.args[3];
                        int optStat = (int) param.args[4];
                        String troopQuestion = (String) param.args[5];
                        String troopAnswer = (String) param.args[6];
                        String returnAddr = (String) param.args[7];
                        String lastActivity = (String) param.args[8];

                        XposedBridge.log("--Add Group Not AuthKey---------------------------------------------------------------");
                        XposedBridge.log("uin=" + uin);
                        XposedBridge.log("nickName=" + nickName);
                        XposedBridge.log("groupOption=" + groupOption);
                        XposedBridge.log("optStat=" + optStat);
                        XposedBridge.log("troopQuestion=" + troopQuestion);
                        XposedBridge.log("troopAnswer=" + troopAnswer);
                        XposedBridge.log("returnAddr=" + returnAddr);
                        XposedBridge.log("lastActivity=" + lastActivity);
                        XposedBridge.log("------------------------------------------------------------------------------------");
                    }
                });

        //添加群Intent
        findAndHookMethod("com.tencent.mobileqq.activity.AddFriendLogicActivity", loadPackageParam.classLoader, "a",
                "android.content.Context",
                String.class,
                String.class,
                short.class,
                int.class,
                String.class,
                String.class,
                String.class,
                String.class,
                String.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        String uin = (String) param.args[1];
                        String nickName = (String) param.args[2];
                        short groupOption = (short) param.args[3];
                        int optStat = (int) param.args[4];
                        String troopQuestion = (String) param.args[5];
                        String troopAnswer = (String) param.args[6];
                        String returnAddr = (String) param.args[7];
                        String lastActivity = (String) param.args[8];
                        String authKey = (String) param.args[9];

                        XposedBridge.log("--Add Group Exist AuthKey------------------------------------------------------------");
                        XposedBridge.log("uin=" + uin);
                        XposedBridge.log("nickName=" + nickName);
                        XposedBridge.log("groupOption=" + groupOption);
                        XposedBridge.log("optStat=" + optStat);
                        XposedBridge.log("troopQuestion=" + troopQuestion);
                        XposedBridge.log("troopAnswer=" + troopAnswer);
                        XposedBridge.log("returnAddr=" + returnAddr);
                        XposedBridge.log("lastActivity=" + lastActivity);
                        XposedBridge.log("authKey=" + authKey);
                        XposedBridge.log("------------------------------------------------------------------------------------");
                    }
                });

        //发送加群申请
        final Class<?> clazz = XposedHelpers.findClass("com.tencent.mobileqq.activity.AddFriendVerifyActivity", loadPackageParam.classLoader);
        findAndHookMethod("com.tencent.mobileqq.activity.AddFriendVerifyActivity", loadPackageParam.classLoader, "a",
                String.class,
                String.class,
                int.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        String troopUin = (String) param.args[0];
                        String content = (String) param.args[1];
                        int optStat = (int) param.args[2];

                        String authKey = (String) findField(clazz, "j").get(param.thisObject);
                        int int_K = (int) findField(clazz, "k").get(param.thisObject);
                        String str_K = (String) findField(clazz, "k").get(param.thisObject);
                        String str_L = (String) findField(clazz, "l").get(param.thisObject);

                        XposedBridge.log("--AddFriendVerifyActivity------------------------------------------------------------");
                        XposedBridge.log("authKey=" + authKey);
                        XposedBridge.log("int_K=" + int_K);
                        XposedBridge.log("str_K=" + str_K);
                        XposedBridge.log("str_L(picUrl)=" + str_L);
                        XposedBridge.log("troopUin=" + troopUin);
                        XposedBridge.log("content=" + content);
                        XposedBridge.log("optStat=" + optStat);
                        XposedBridge.log("------------------------------------------------------------------------------------");
                    }
                });
    }

    /**
     * 接收消息
     */
    public class ReceiverSQLiteHelperAsync extends AsyncTask<FromServiceMsg, Void, Void> {

        private ServiceMsgSQLiteHelper mServiceMsgSQLiteHelper;

        public ReceiverSQLiteHelperAsync() {
            if (mServiceMsgSQLiteHelper == null) {
                mServiceMsgSQLiteHelper = new ServiceMsgSQLiteHelper(new DatabaseContext(MyApplication.applicationContext));
            }
        }

        @Override
        protected Void doInBackground(FromServiceMsg... params) {
            mServiceMsgSQLiteHelper.insert(params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mServiceMsgSQLiteHelper.close();
        }
    }


    /**
     * 发送消息
     */
    public class SenderSQLiteHelperAsync extends AsyncTask<ToServiceMsg, Void, Void> {

        private ServiceMsgSQLiteHelper mServiceMsgSQLiteHelper;

        public SenderSQLiteHelperAsync() {
            if (mServiceMsgSQLiteHelper == null) {
                mServiceMsgSQLiteHelper = new ServiceMsgSQLiteHelper(new DatabaseContext(MyApplication.applicationContext));
            }
        }

        @Override
        protected Void doInBackground(ToServiceMsg... params) {
            ToServiceMsg msg = params[0];
            mServiceMsgSQLiteHelper.insert(msg);
            if (!TextUtils.isEmpty(msg.serviceCmd)) {
                mServiceMsgSQLiteHelper.insert(ServiceCmd.getInstance(msg.serviceCmd, msg.needResp));
            }
            if (!TextUtils.isEmpty(msg.msfCommand)) {
                mServiceMsgSQLiteHelper.insert(MSFCmd.getInstance(msg.msfCommand, msg.needResp));
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mServiceMsgSQLiteHelper.close();
        }
    }

}