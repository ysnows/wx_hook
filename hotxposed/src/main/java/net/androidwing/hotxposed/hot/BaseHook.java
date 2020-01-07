package net.androidwing.hotxposed.hot;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.widget.Toast;

import net.androidwing.hotxposed.log.Logs;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class BaseHook implements IXposedHookLoadPackage, IHookerDispatcher, Application.ActivityLifecycleCallbacks {
    public Activity focusActivity;
    protected XC_LoadPackage.LoadPackageParam packageParam;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        //startHotXPosed(loadPackageParam);
    }

    protected void startHotXPosed(Class clz, XC_LoadPackage.LoadPackageParam loadPackageParam, String localePackage, String[] targetPackages) {
        if (!loadPackageParam.packageName.equals("android")) {

            for (String targetPackage : targetPackages) {
                if (loadPackageParam.packageName.equals(targetPackage)) {
                    try {
                        HotXPosed.hook(clz, loadPackageParam, localePackage);
                    } catch (Exception e) {
                        //Logs.e(e);
                    }
                } else if (loadPackageParam.packageName.equals(localePackage)) {
                    XposedHelpers.findAndHookMethod("z.houbin.xposed.lib.XposedUtil", loadPackageParam.classLoader, "isHook", XC_MethodReplacement.returnConstant(true));
                }
            }

        }
    }

    protected void startHotXPosed(Class clz, XC_LoadPackage.LoadPackageParam loadPackageParam, String localePackage) {
        if (!loadPackageParam.packageName.equals("android")) {
            if (loadPackageParam.packageName.equals(localePackage)) {
                XposedHelpers.findAndHookMethod("z.houbin.xposed.lib.XposedUtil", loadPackageParam.classLoader, "isHook", XC_MethodReplacement.returnConstant(true));
            } else {
                try {
                    HotXPosed.hook(clz, loadPackageParam, localePackage);
                } catch (Exception e) {
                    //Logs.e(e);
                }
            }
        }
    }

    @Override
    public void dispatch(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        //Config.init(loadPackageParam.packageName);
        this.packageParam = loadPackageParam;
    }

    public void dispatchAttach(Application application) {
        application.registerActivityLifecycleCallbacks(this);
    }

    public ClassLoader getClassLoader() {
        if (this.packageParam != null) {
            return this.packageParam.classLoader;
        }

        if (focusActivity != null) {
            return focusActivity.getClassLoader();
        }

        return null;
    }

    public Class load(String name) {
        ClassLoader classLoader = getClassLoader();
        if (classLoader != null) {
            try {
                return classLoader.loadClass(name);
            } catch (ClassNotFoundException e) {
                Logs.e(e);
            }
        }
        return null;
    }

    public void toast(final String message) {
        if (focusActivity != null) {
            focusActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(focusActivity, String.valueOf(message), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void toastLong(final String message) {
        if (focusActivity != null) {
            focusActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(focusActivity, String.valueOf(message), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        focusActivity = activity;
    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}
