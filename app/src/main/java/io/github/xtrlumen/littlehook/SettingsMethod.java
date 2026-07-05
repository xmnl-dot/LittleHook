package io.github.xtrlumen.littlehook;

import android.content.Context;

import android.util.Log;

import java.util.List;

import java.lang.reflect.Method;

import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam;

import static io.github.xtrlumen.littlehook.Entry.*;

public class SettingsMethod {
    private static final String CLASS = "[SettingsMethod] ";

    public void onPackageReady(XposedModule XposedBridge, PackageReadyParam param, ClassLoader classLoader) {
        if (!(system_settings_unlock_google_header || show_color_advanced)) {
            XposedBridge.log(Log.DEBUG, TAG, CLASS + "Ignored Hook");
            return;
        }
        // 禁止隐藏Google入口
        if (system_settings_unlock_google_header) try {
            Class<?> mMiuiSettings = classLoader.loadClass("com.android.settings.MiuiSettings");
            Method targetMethod = mMiuiSettings.getDeclaredMethod(
                "updateHeaderList",
                List.class
            );
            final Method ADD_GOOGLE = mMiuiSettings.getDeclaredMethod(
                "AddGoogleSettingsHeaders",
                List.class
            );
            ADD_GOOGLE.setAccessible(true);

            XposedBridge.hook(targetMethod).intercept(chain -> {
                try {
                    return chain.proceed();
                } finally {
                    List settingsList = (List) chain.getArg(0);
                    ADD_GOOGLE.invoke(chain.getThisObject(), settingsList);
                }
            });
        } catch (Throwable t) {
            XposedBridge.log(Log.ERROR, TAG, CLASS + "Prohibit hide Google entry Module Hook failed: ", t);
        }
        // 显示色彩风格高级模式
        if (show_color_advanced) {
            hookShowColorAdvancedMode(XposedBridge, classLoader);
        }
        XposedBridge.log(Log.DEBUG, TAG, CLASS + "Hooked");
    }

    /**
     * 显示色彩风格高级模式
     * Hook isSupportSimplifiedColormode -> false, 解锁完整色彩模式选项
     */
    private void hookShowColorAdvancedMode(XposedModule XposedBridge, ClassLoader classLoader) {
        try {
            Class<?> screenEffect = classLoader.loadClass("com.android.settings.display.ScreenEffectFragment");
            Method m = screenEffect.getDeclaredMethod("isSupportSimplifiedColormode");
            XposedBridge.hook(m).intercept(chain -> {
                XposedBridge.log(Log.DEBUG, TAG, CLASS + "isSupportSimplifiedColormode -> false");
                return false;
            });
            XposedBridge.log(Log.DEBUG, TAG, CLASS + "Hooked isSupportSimplifiedColormode");
        } catch (Throwable t) {
            XposedBridge.log(Log.ERROR, TAG, CLASS + "hookShowColorAdvancedMode failed: ", t);
        }
    }
}
