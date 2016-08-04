package me.hellojuly.xposedhelper.utils;

import android.app.ActivityManager;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;

import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import me.hellojuly.xposedhelper.Config;

import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

/**
 * Desc: 将此APP从列表中隐藏
 * Created by 庞承晖
 * Date: 2016/8/4.
 * Time: 15:39
 */
public class HidePackageUtil {

    private static String mHidePackageName = "";

    /**
     * 将此APP从列表中隐藏
     *
     * @param loadPackageParam
     */
    public static void hideModule(XC_LoadPackage.LoadPackageParam loadPackageParam, String packageName) {
        mHidePackageName = packageName;

        //获取应用信息
        findAndHookMethod("android.app.ApplicationPackageManager", loadPackageParam.classLoader, "getApplicationInfo", String.class, int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                String packageName = (String) param.args[0];
                if (isTarget(packageName)) {
                    param.args[0] = Config.PACKAGE_NAME_QQ;
                    log("Fake package: " + packageName + " as " + Config.PACKAGE_NAME_QQ);
                }
            }
        });

        //获取包信息
        findAndHookMethod("android.app.ApplicationPackageManager", loadPackageParam.classLoader, "getPackageInfo", String.class, int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                String packageName = (String) param.args[0];
                if (isTarget(packageName)) {
                    param.args[0] = Config.PACKAGE_NAME_QQ;
                    log("Fake package: " + packageName + " as " + Config.PACKAGE_NAME_QQ);
                }
            }
        });

        //已安装的应用列表
        findAndHookMethod("android.app.ApplicationPackageManager", loadPackageParam.classLoader, "getInstalledApplications", int.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                List<ApplicationInfo> applicationList = (List) param.getResult();
                List<ApplicationInfo> resultApplicationList = new ArrayList<>();

                for (ApplicationInfo applicationInfo : applicationList) {
                    String packageName = applicationInfo.packageName;
                    if (isTarget(packageName)) {
                        log("Hid package: " + packageName);
                    } else {
                        resultApplicationList.add(applicationInfo);
                    }
                }
                param.setResult(resultApplicationList);
            }
        });
        //已安装的包列表
        findAndHookMethod("android.app.ApplicationPackageManager", loadPackageParam.classLoader, "getInstalledPackages", int.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                List<PackageInfo> packageInfoList = (List) param.getResult();
                List<PackageInfo> resultPackageInfoList = new ArrayList<>();

                for (PackageInfo packageInfo : packageInfoList) {
                    String packageName = packageInfo.packageName;
                    if (isTarget(packageName)) {
                        log("Hid package: " + packageName);
                    } else {
                        resultPackageInfoList.add(packageInfo);
                    }
                }
                param.setResult(resultPackageInfoList);
            }
        });

        //获取正在运行的服务列表
        findAndHookMethod("android.app.ActivityManager", loadPackageParam.classLoader, "getRunningServices", int.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                List<ActivityManager.RunningServiceInfo> serviceList = (List) param.getResult();
                List<ActivityManager.RunningServiceInfo> resultServiceList = new ArrayList<>();

                for (ActivityManager.RunningServiceInfo runningServiceInfo : serviceList) {
                    String serviceName = runningServiceInfo.process;
                    if (isTarget(serviceName)) {
                        log("Hid service: " + serviceName);
                    } else {
                        resultServiceList.add(runningServiceInfo);
                    }
                }
                param.setResult(resultServiceList);
            }
        });

        //获取正在运行的任务列表
        findAndHookMethod("android.app.ActivityManager", loadPackageParam.classLoader, "getRunningTasks", int.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                List<ActivityManager.RunningTaskInfo> serviceList = (List) param.getResult();
                List<ActivityManager.RunningTaskInfo> resultServiceList = new ArrayList<>();

                for (ActivityManager.RunningTaskInfo runningTaskInfo : serviceList) {
                    String taskName = runningTaskInfo.baseActivity.flattenToString();
                    if (isTarget(taskName)) {
                        log("Hid task: " + taskName);
                    } else {
                        resultServiceList.add(runningTaskInfo);
                    }
                }
                param.setResult(resultServiceList);
            }
        });

        //获取正在运行的APP进程列表
        findAndHookMethod("android.app.ActivityManager", loadPackageParam.classLoader, "getRunningAppProcesses", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                List<ActivityManager.RunningAppProcessInfo> runningAppProcessList = (List) param.getResult();
                List<ActivityManager.RunningAppProcessInfo> resultList = new ArrayList<>();

                for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : runningAppProcessList) {
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

    private static boolean isTarget(String name) {
        return name.contains(mHidePackageName) || name.contains("xposed");
    }
}
