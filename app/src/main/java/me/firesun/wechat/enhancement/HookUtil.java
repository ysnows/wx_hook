package me.firesun.wechat.enhancement;

import net.androidwing.hotxposed.HotXposed;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by wing on 4/18/17.
 */

public class HookUtil implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam loadPackageParam)
            throws Throwable {

        HotXposed.hook(Main.class, loadPackageParam);
    }
}
