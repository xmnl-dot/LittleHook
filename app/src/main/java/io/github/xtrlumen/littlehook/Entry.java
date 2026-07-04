package io.github.xtrlumen.littlehook;

import android.util.Log;

import io.github.libxposed.api.XposedModule;

public class Entry extends XposedModule {
    static final String TAG = "LittleHook";
    static final boolean
        html_viewer_disable_cloud_control    = true,  // 禁用HTML查看器云控
        incallui_answer_in_head_up           = true,  // 从浮动通知接听电话时不进入全屏
        incallui_answer_in_head_up_desktop   = false, // 上方附属设置:位于桌面时依旧进入全屏
        various_fuck_xlDownload              = true,  // 阻止创建.xlDownload文件夹
        system_settings_unlock_google_header = true,  // 禁止隐藏Google入口
        leica_theme                          = true,  // 使 Xiaomi 17 Ultra 标准版识别徕卡版定制主题
        lbe_auto_start                       = true,  // 禁止自动关闭自启动
        disable_root_check                   = true,  // 禁用环境检查
        disable_flag_secure                  = true,  // 阻止自动上传已安装应用列表
        desktop_hide_clear_button            = true,  // 隐藏最近任务界面清理按钮
        desktop_real_memory_usage            = true,  // 最近任务界面显示内存真实用量
        desktop_prestart                     = true,  // 禁用系统桌面触碰图标时预加载应用
        desktop_recent_text                  = true,  // 自定义最近任务界面无后台时显示的文本
        splash_screen                        = true,  // 彻底关闭 Splash Screen
        native_file_picker                   = true,  // 全局伪装开发者相关选项为关闭
        disable_upload_applist               = true,  // 强制原生文件选择器
        adb_developer_hide                   = true,  // 在不允许截图的应用中强制允许截图
        package_installer                    = true;  // 恢复并锁定原生软件包安装器
    static final String
        desktop_custom_recent_text = "";
    @Override
    public void onModuleLoaded(ModuleLoadedParam param) {
    }
    @Override
    public void onPackageLoaded(PackageLoadedParam param) {
    }
    @Override
    public void onPackageReady(PackageReadyParam param) {
        String
            packageName = param.getPackageName(),
            onTiming = "onPackageReady";
        ClassLoader
            classLoader = param.getClassLoader();
        switch (packageName) {
            case "com.android.htmlviewer":
                log(Log.DEBUG, TAG, "Loaded into " + packageName + " From " + onTiming);
                new HtmlViewerMethod().onPackageReady(this, param, classLoader);
                break;
            case "com.android.incallui":
                log(Log.DEBUG, TAG, "Loaded into " + packageName + " From " + onTiming);
                new InCallUiMethod().onPackageReady(this, param, classLoader);
                break;
            case "com.android.photopicker":
                log(Log.DEBUG, TAG, "Loaded into " + packageName + " From " + onTiming);
                new PhotoPickerMethod().onPackageReady(this, param, classLoader);
                break;
            case "com.android.providers.downloads":
                log(Log.DEBUG, TAG, "Loaded into " + packageName + " From " + onTiming);
                new DownloadsMethod().onPackageReady(this, param, classLoader);
                break;
            case "com.android.settings":
                log(Log.DEBUG, TAG, "Loaded into " + packageName + " From " + onTiming);
                new SettingsMethod().onPackageReady(this, param, classLoader);
                break;
            case "com.android.systemui":
                log(Log.DEBUG, TAG, "Loaded into " + packageName + " From " + onTiming);
                new SystemUiMethod().onPackageReady(this, param, classLoader);
                break;
            case "com.android.thememanager":
                log(Log.DEBUG, TAG, "Loaded into " + packageName + " From " + onTiming);
                new ThemeGlobal().onPackageReady(this, param, classLoader);
                break;
            case "com.lbe.security.miui":
                log(Log.DEBUG, TAG, "Loaded into " + packageName + " From " + onTiming);
                new LbeSecurityMethod().onPackageReady(this, param, classLoader);
                break;
            case "com.miui.guardprovider":
                log(Log.DEBUG, TAG, "Loaded into " + packageName + " From " + onTiming);
                new GuardProviderMethod().onPackageReady(this, param, classLoader);
                break;
            case "com.miui.home":
                log(Log.DEBUG, TAG, "Loaded into " + packageName + " From " + onTiming);
                new DesktopGlobal().onPackageReady(this, param, classLoader);
                new DesktopMethod().onPackageReady(this, param, classLoader);
                break;
            case "com.miui.securitycore":
                log(Log.DEBUG, TAG, "Loaded into " + packageName + " From " + onTiming);
                new SecurityCoreMethod().onPackageReady(this, param, classLoader);
                break;
            default:
                log(Log.DEBUG, TAG, "Ignored " + packageName + " From " + onTiming);
                break;
        }
    }
    @Override
    public void onSystemServerStarting(SystemServerStartingParam param) {
        String onTiming = "onSystemServerStarting";
        ClassLoader classLoader = param.getClassLoader();
        log(Log.DEBUG, TAG, "Loaded into system From " + onTiming);
        new FrameworkMethod().onSystemServerStarting(this, param, classLoader);
        new FrameworkGlobal().onSystemServerStarting(this, param, classLoader);
    }
}