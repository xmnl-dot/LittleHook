package io.github.xtrlumen.littlehook;

import android.util.Log;

import java.lang.reflect.Method;

import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam;

import static io.github.xtrlumen.littlehook.Entry.*;

public class ThemeGlobal {
    private static final String CLASS = "[ThemeGlobal] ";
    public void onPackageReady(XposedModule XposedBridge, PackageReadyParam param) {
        if (!(leica_theme)) {
            XposedBridge.log(Log.DEBUG, TAG, CLASS + "Ignored Hook");
            return;
        }
        if (leica_theme) try {
            Class<?> targetClass = param.getClassLoader().loadClass(
                "android.os.SystemProperties"
            );
            Method targetMethod = targetClass.getDeclaredMethod(
                "get",
                String.class,
                String.class
            );
            XposedBridge.hook(targetMethod).intercept(chain -> {
                Object result = chain.proceed();
                if ("ro.boot.product.theme_customize".equals(chain.getArg(0))) {
                    result = "P1_Leica";
                    XposedBridge.log(Log.DEBUG, TAG, CLASS + "ro.boot.product.theme_customize -> P1_Leica");
                }
                return result;
            });
        } catch (Throwable t) {
            XposedBridge.log(Log.ERROR, TAG, CLASS + "'android.os.SystemProperties.get' Module Hook failed: ", t);
        }
        XposedBridge.log(Log.DEBUG, TAG, CLASS + "Hooked");
    }
}