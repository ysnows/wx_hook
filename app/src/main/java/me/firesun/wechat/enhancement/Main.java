package me.firesun.wechat.enhancement;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import net.androidwing.hotxposed.debug.DebugListner;
import net.androidwing.hotxposed.debug.Trace;
import net.androidwing.hotxposed.hot.BaseHook;
import net.androidwing.hotxposed.log.Logs;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import me.firesun.wechat.enhancement.plugin.ADBlock;
import me.firesun.wechat.enhancement.plugin.AntiRevoke;
import me.firesun.wechat.enhancement.plugin.AntiSnsDelete;
import me.firesun.wechat.enhancement.plugin.AutoLogin;
import me.firesun.wechat.enhancement.plugin.HideModule;
import me.firesun.wechat.enhancement.plugin.IPlugin;
import me.firesun.wechat.enhancement.plugin.Limits;
import me.firesun.wechat.enhancement.plugin.LuckMoney;
import me.firesun.wechat.enhancement.util.HookParams;
import me.firesun.wechat.enhancement.util.SearchClasses;

import static de.robv.android.xposed.XposedBridge.log;


public class Main extends BaseHook {

    private static IPlugin[] plugins = {
            new ADBlock(),
            new AntiRevoke(),
            new AntiSnsDelete(),
            new AutoLogin(),
            new HideModule(),
            new LuckMoney(),
            new Limits(),
    };

    private static final String LOCALE_PACKAGE = BuildConfig.APPLICATION_ID;
    public static final String[] TARGET_PACKAGES = {"com.wingsofts.zoomimageheader", "com.tencent.mm", "com.tencent.mm:tools"};

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        startHotXPosed(Main.class, loadPackageParam, LOCALE_PACKAGE, TARGET_PACKAGES);
    }

    @Override
    public void dispatch(final LoadPackageParam lpparam) {

        XposedBridge.log("Main hook dispatch ->" + lpparam.packageName + "-" + LOCALE_PACKAGE + "-" + lpparam.packageName);
        Logs.init("z.houbin.lib.test");
        XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);

                Logs.printMethodParam("attach", param);

                Application context = (Application) param.thisObject;

                dispatchAttach(context);

            }
        });

        try {
            XposedHelpers.findAndHookMethod(ContextWrapper.class, "attachBaseContext", Context.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    Context context = (Context) param.args[0];
                    String processName = lpparam.processName;
                    //Only hook important process

                    String versionName = getVersionName(context, HookParams.WECHAT_PACKAGE_NAME);
                    String text = "wechat version:" + versionName;
                    log(text);

                    Toast.makeText(context, text, Toast.LENGTH_SHORT).show();

                    if (!HookParams.hasInstance()) {
                        SearchClasses.init(context, lpparam, versionName);
                        loadPlugins(lpparam);
                    }
                }
            });
        } catch (Error | Exception e) {
        }
    }


    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {
        super.onActivityCreated(activity, bundle);

        Toast.makeText(activity, "hello", Toast.LENGTH_SHORT).show();
        Trace.traceClass(activity.getClass(), new DebugListner());

    }

    private String getVersionName(Context context, String packageName) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packInfo = packageManager.getPackageInfo(packageName, 0);
            return packInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
        }
        return "";
    }


    private void loadPlugins(LoadPackageParam lpparam) {
        for (IPlugin plugin : plugins) {
            try {
                plugin.hook(lpparam);
            } catch (Error | Exception e) {
                log("loadPlugins error" + e);
            }
        }
    }


}
