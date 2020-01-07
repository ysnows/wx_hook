package net.androidwing.hotxposed.hot;

import android.text.TextUtils;

import net.androidwing.hotxposed.shell.Shell;

import java.io.File;

import dalvik.system.PathClassLoader;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HotXPosed {

    public static void hook(Class clazz, XC_LoadPackage.LoadPackageParam lpparam, String packageName) {
        try {
            File apkFile = getApkFile(packageName);
            if (apkFile == null || !apkFile.exists()) {
                XposedBridge.log("apk file not found,hot load :" + packageName);
                return;
            }
            PathClassLoader classLoader = new PathClassLoader(apkFile.getAbsolutePath(), lpparam.getClass().getClassLoader());
            XposedHelpers.callMethod(classLoader.loadClass(clazz.getName()).newInstance(), "dispatch", lpparam);
        } catch (Exception e) {
            //Logs.e(e);
        }
    }

    private static File getApkFile(String packageName) {
        String filePath = Shell.run("pm path " + packageName).getStdout();
        XposedBridge.log(filePath);
        if (TextUtils.isEmpty(filePath)) {
            return null;
        }
        filePath = filePath.substring(filePath.indexOf(":") + 1);
        XposedBridge.log(filePath);
        File apkFile = new File(filePath);
        if (!apkFile.exists()) {
            filePath = String.format("/data/app/%s-%s/base.apk", packageName, 2);
            apkFile = new File(filePath);
        }
        return apkFile;
    }
}
