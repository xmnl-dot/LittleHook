package io.github.xtrlumen.littlehook;

import android.util.Log;
import android.widget.TextView;

import android.app.Activity;
import android.app.AlertDialog;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam;

import static io.github.xtrlumen.littlehook.Entry.*;

public class SystemUiMethod {
    private static final String CLASS = "[SystemUiMethod] ";
    private static final String CLOCK_FORMAT = "aahh:mm";
    public void onPackageReady(XposedModule XposedBridge, PackageReadyParam param, ClassLoader classLoader) {
        // 修改状态栏时钟格式为 "aahh:mm"
        try {
            Class<?> miuiClockClass = classLoader.loadClass("com.android.systemui.statusbar.views.MiuiClock");
            Method updateTimeMethod = miuiClockClass.getDeclaredMethod("updateTime");
            XposedBridge.hook(updateTimeMethod).intercept(chain -> {
                TextView textView = (TextView) chain.getThisObject();
                String clockName = textView.getResources().getResourceEntryName(textView.getId());
                if ("clock".equals(clockName)) {
                    try {
                        Field controllerField = textView.getClass().getDeclaredField("mMiuiStatusBarClockController");
                        controllerField.setAccessible(true);
                        Object controller = controllerField.get(textView);
                        if (controller != null) {
                            Field calendarField = controller.getClass().getDeclaredField("mCalendar");
                            calendarField.setAccessible(true);
                            Object calendar = calendarField.get(controller);
                            if (calendar != null) {
                                SimpleDateFormat sdf = new SimpleDateFormat(CLOCK_FORMAT, Locale.getDefault());
                                String formattedTime = sdf.format(new Date());
                                textView.post(() -> textView.setText(formattedTime));
                                return null;
                            }
                        }
                    } catch (Throwable t) {
                        XposedBridge.log(Log.ERROR, TAG, CLASS + "'clock' format hook failed, using fallback: ", t);
                        SimpleDateFormat sdf = new SimpleDateFormat(CLOCK_FORMAT, Locale.getDefault());
                        String formattedTime = sdf.format(new Date());
                        textView.post(() -> textView.setText(formattedTime));
                        return null;
                    }
                }
                return chain.proceed();
            });
            XposedBridge.log(Log.DEBUG, TAG, CLASS + "Clock format hook installed");
        } catch (Throwable t) {
            XposedBridge.log(Log.ERROR, TAG, CLASS + "'clock' Module Hook failed: ", t);
        }
        // 在不允许截图的应用中强制允许截图
        if (disable_flag_secure) try {
            Method method = Activity.class.getDeclaredMethod("onResume");
            XposedBridge.hook(method).intercept(chain -> {
                Activity activity = (Activity) chain.getThisObject();
                new AlertDialog.Builder(activity)
                    .setTitle("Enable Screenshot")
                    .setMessage("Incorrect module usage, remove this app from scope.")
                    .setCancelable(false)
                    .setPositiveButton("OK", (dialog, which) -> System.exit(0))
                    .show();
                return chain.proceed();
            });
        } catch (Throwable t) {
            XposedBridge.log(Log.ERROR, TAG, CLASS + "'disable_flag_secure' Module Hook failed: ", t);
        }
        XposedBridge.log(Log.DEBUG, TAG, CLASS + "Hooked");
    }
}
