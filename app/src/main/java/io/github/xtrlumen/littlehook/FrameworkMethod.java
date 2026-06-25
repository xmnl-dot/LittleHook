package io.github.xtrlumen.littlehook;

import android.util.Log;
import android.content.Intent;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedModuleInterface.SystemServerStartingParam;

import static io.github.xtrlumen.littlehook.Entry.*;

@SuppressWarnings("unchecked")
public class FrameworkMethod {
    private static final String CLASS = "[FrameworkMethod] ";
    public void onSystemServerStarting(XposedModule XposedBridge, SystemServerStartingParam param) {
        if (!(package_installer || splash_screen)) {
            XposedBridge.log(Log.DEBUG, TAG, CLASS + "Ignored Hook");
            return;
        }
        // 恢复并锁定原生软件包安装器
        if (package_installer) try {
            Class<?> packageManagerServiceImpl = param.getClassLoader().loadClass(
                "com.android.server.pm.PackageManagerServiceImpl"
            );
            Field finalFCurrentPackageInstaller = packageManagerServiceImpl.getDeclaredField(
                "mCurrentPackageInstaller"
            );
            finalFCurrentPackageInstaller.setAccessible(true);
            AtomicBoolean fakeCTS = new AtomicBoolean(false);
            AtomicReference<String> mCurrentPackageInstaller = new AtomicReference<>("");
            for (Method targetMethod : packageManagerServiceImpl.getDeclaredMethods()) {
                String methodName = targetMethod.getName();
                if ("updateDefaultPkgInstallerLocked".equals(methodName)) {
                    XposedBridge.hook(targetMethod).intercept(chain -> {
                        fakeCTS.set(true);
                        if (finalFCurrentPackageInstaller != null && mCurrentPackageInstaller.get().isEmpty() && finalFCurrentPackageInstaller.get(chain.getThisObject()) instanceof String currentPackageInstaller) {
                            mCurrentPackageInstaller.compareAndSet("", currentPackageInstaller);
                        }
                        try {
                            return chain.proceed();
                        } finally {
                            fakeCTS.set(false);
                        }
                    });
                    XposedBridge.deoptimize(targetMethod);
                }
                if ("assertValidApkAndInstaller".equals(methodName)) {
                    XposedBridge.hook(targetMethod).intercept(chain -> {
                        fakeCTS.set(true);
                        try {
                            return chain.proceed();
                        } finally {
                            fakeCTS.set(false);
                        }
                    });
                    XposedBridge.deoptimize(targetMethod);
                }
                if ("hookChooseBestActivity".equals(methodName)) {
                    XposedBridge.hook(targetMethod).intercept(chain -> {
                        try {
                            if (chain.getArg(0) instanceof Intent intent) {
                                if (finalFCurrentPackageInstaller != null && "application/vnd.android.package-archive".equals(intent.getType()) && Intent.ACTION_VIEW.equals(intent.getAction())) {
                                    fakeCTS.set(true);
                                }
                            }
                            return chain.proceed();
                        } finally {
                            fakeCTS.set(false);
                        }
                    });
                    XposedBridge.deoptimize(targetMethod);
                }
            }
            try {
                Method isCTSMethod = packageManagerServiceImpl.getDeclaredMethod("isCTS");
                XposedBridge.hook(isCTSMethod).intercept(chain -> {
                    if (fakeCTS.get()) {
                        return true;
                    }
                    return chain.proceed();
                });
            } catch (Throwable t) {
                XposedBridge.log(Log.ERROR, TAG, CLASS + "Failed to hook isCTS", t);
            }
            try {
                Class<?> XSpaceConstantClass = param.getClassLoader().loadClass(
                    "miui.securityspace.XSpaceConstant"
                );
                Field requiredAppsField = XSpaceConstantClass.getDeclaredField(
                    "REQUIRED_APPS"
                );
                requiredAppsField.setAccessible(true);
                ArrayList<String> requiredApps = (ArrayList<String>) requiredAppsField.get(null);
                if (requiredApps != null && !requiredApps.contains("com.android.packageinstaller")) {
                    requiredApps.add("com.android.packageinstaller");
                }
            } catch (Throwable t) {
                XposedBridge.log(Log.ERROR, TAG, CLASS + "Failed to add package installer to REQUIRED_APPS", t);
            }
        } catch (Throwable t) {
            XposedBridge.log(Log.ERROR, TAG, CLASS + "'package_installer' Module Hook failed: ", t);
        }
        // 彻底关闭 Splash Screen
        if (splash_screen) try {
            Class<?> activityRecordClass = param.getClassLoader().loadClass(
                "com.android.server.wm.ActivityRecord"
            );
            for (Method targetMethod : activityRecordClass.getDeclaredMethods()) {
                if (targetMethod.getName().equals("showStartingWindow") && targetMethod.getParameterCount() == 7) {
                    XposedBridge.hook(targetMethod).intercept(chain -> {
                        return null;
                    });
                    break;
                }
            }
        } catch (Throwable t) {
            XposedBridge.log(Log.ERROR, TAG, CLASS + "'splash_screen' Module Hook failed: ", t);
        }
        XposedBridge.log(Log.DEBUG, TAG, CLASS + "Hooked");
    }
}