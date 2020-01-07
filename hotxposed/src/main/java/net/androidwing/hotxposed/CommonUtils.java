package net.androidwing.hotxposed;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import static android.content.Context.MODE_WORLD_READABLE;

public class CommonUtils {
    private Context activity;
    private static CommonUtils commonUtils;

    public CommonUtils with(Context activity) {
        this.activity = activity;
        return this;
    }

    public void initAppPath() {
        String preferenceName = activity.getPackageName() + "_preferences";

        activity.getSharedPreferences(preferenceName, MODE_WORLD_READABLE)
                .edit()
                .putString("not_contains", activity.getApplicationContext().getPackageResourcePath())
                .apply();
    }


    public static void restartTargetApp(String packageName, String startActivity) {
        ShellUtil.execCommand("am force-stop " + packageName, true);
        String startCommand = String.format("am start -n \"%s/%s.%s\"", packageName, packageName, startActivity);
        Log.d(packageName, startCommand);
        ShellUtil.execCommand(startCommand, true);
    }
}
