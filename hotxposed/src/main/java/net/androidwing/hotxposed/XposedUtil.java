package net.androidwing.hotxposed;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.Process;
import android.support.annotation.RequiresApi;
import android.util.ArrayMap;
import android.widget.Toast;

import net.androidwing.hotxposed.log.Logs;
import net.androidwing.hotxposed.shell.Shell;
import net.androidwing.hotxposed.thread.ThreadPool;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class XposedUtil {

    /**
     * md5加密
     *
     * @param s 要加密的字符串
     * @return 加密结果
     */
    public static String md5(String s) {
        char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        try {
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            byte[] md = mdInst.digest(s.getBytes());
            int j = md.length;
            char[] str = new char[j * 2];
            int k = 0;
            for (byte b : md) {
                str[k++] = hexDigits[b >>> 4 & 0xf];
                str[k++] = hexDigits[b & 0xf];
            }
            return new String(str).toLowerCase();
        } catch (Exception e) {
            Logs.e(e);
            return "";
        }
    }

    /**
     * 判断是否主进程
     *
     * @param context 上下文
     * @return true 主进程
     */
    public static boolean isMainProcess(Context context) {
        try {
            int pid = Process.myPid();
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (activityManager == null) {
                return true;
            }
            List list = activityManager.getRunningAppProcesses();
            for (Object o : list) {
                ActivityManager.RunningAppProcessInfo info = (ActivityManager.RunningAppProcessInfo) (o);
                try {
                    if (info.pid == pid) {
                        // 根据进程的信息获取当前进程的名字
                        if (info.processName.contains(":")) {
                            return false;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 获取版本名
     *
     * @param context 上下文
     * @return 版本名
     */
    public static String getVersionName(Context context) {
        String verName = "";
        try {
            verName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return verName;
    }

    /**
     * 重启程序
     *
     * @param context     上下文
     * @param packageName 包名
     */
    public static void restartPackage(final Context context, final String packageName) {
        if (context == null) {
            return;
        }
        ThreadPool.post(new Runnable() {
            @Override
            public void run() {
                Shell.SU.run("am force-stop " + packageName);

                Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            }
        });
    }

    /**
     * 重启程序
     *
     * @param context     上下文
     * @param packageName 包名
     */
    public static void restartPackage(final Context context, final String user, final String packageName) {
        if (context == null) {
            return;
        }
        ThreadPool.post(new Runnable() {
            @Override
            public void run() {
                Shell.SU.run("am force-stop --user " + user + " com.tencent.mm");

                Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            }
        });
    }

    /**
     * 判断是否在当前主线程
     */
    public static boolean isMainThread() {
        return Thread.currentThread() == Looper.getMainLooper().getThread();
    }

    /**
     * 获取对象指定顺序的成员变量
     *
     * @param object 对象
     * @param index  位置
     */
    public static Object getField(Object object, int index) {
        Field[] fields = object.getClass().getDeclaredFields();
        if (index <= fields.length && index > 0) {
            try {
                Field f = fields[index];
                f.setAccessible(true);
                return f.get(object);
            } catch (IllegalAccessException e) {
                Logs.e(e);
            }
        }
        return null;
    }

    /**
     * 根据类型名称获取成员变量
     *
     * @param object   对象
     * @param typeName 类型
     * @param name     名称
     */
    public static Object getField(Object object, String typeName, String name) {
        Field[] fields = object.getClass().getDeclaredFields();
        for (Field field : fields) {
            if ((field.getType().getName().equals(typeName)) && (field.getName().equals(name))) {
                try {
                    return field.get(object);
                } catch (IllegalAccessException e) {
                    Logs.e(e);
                }
            }
        }
        return null;
    }

    /**
     * 根据方法名,参数类型,返回类型 查找方法
     *
     * @param object     对象
     * @param methodName 反法名
     * @param returnType 返回类型
     * @param params     参数
     */
    public static Method getMethod(Object object, String methodName, String returnType, Class... params) {
        for (Method method : object.getClass().getDeclaredMethods()) {
            if (method.getReturnType().getName().equals(returnType) && method.getName().equals(methodName)) {
                Class[] parameterTypes = method.getParameterTypes();
                if (params.length != parameterTypes.length) {
                    continue;
                }
                for (int i = 0; i < params.length; i++) {
                    if (params[i] != parameterTypes[i]) {
                        break;
                    }
                }
                method.setAccessible(true);
                return method;
            }
        }
        return null;
    }

    /**
     * 调用方法
     *
     * @param object 对象
     * @param method 方法
     * @param params 参数
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static Object callMethod(Object object, Method method, Object... params) {
        if (method != null) {
            try {
                return method.invoke(object, params);
            } catch (IllegalAccessException | InvocationTargetException e) {
                Logs.e(e);
            }
        }
        return null;
    }

    /**
     * 直接获取字符串变量
     *
     * @param object 对象
     * @param field  变量名
     */
    public static String getString(Object object, String field) {
        return "" + XposedHelpers.getObjectField(object, field);
    }

    /**
     * 设置成员变量
     *
     * @param object    对象
     * @param typeClass 类型
     * @param name      名称
     * @param value     值
     */
    public static void setField(Object object, Class typeClass, String name, Object value) {
        Field[] fields = object.getClass().getDeclaredFields();
        for (Field field : fields) {
            if ((field.getType() == typeClass) && (field.getName().equals(name))) {
                field.setAccessible(true);
                try {
                    if (value instanceof Integer) {
                        field.setInt(object, (Integer) value);
                    } else if (value instanceof Long) {
                        field.setLong(object, (Long) value);
                    } else if (value instanceof Boolean) {
                        field.setBoolean(object, (Boolean) value);
                    } else {
                        field.set(object, value);
                    }
                } catch (IllegalAccessException e) {
                    Logs.e(e);
                }
                break;
            }
        }
    }

    /**
     * 设置成员变量
     *
     * @param object 对象
     * @param index  顺序
     * @param value  值
     */
    public static void setField(Object object, int index, Object value) {
        Field[] fields = object.getClass().getDeclaredFields();
        Field f = fields[index];
        f.setAccessible(true);
        try {
            if (value instanceof Integer) {
                f.setInt(object, (Integer) value);
            } else if (value instanceof Long) {
                f.setLong(object, (Long) value);
            } else if (value instanceof Boolean) {
                f.setBoolean(object, (Boolean) value);
            } else {
                f.set(object, value);
            }
        } catch (IllegalAccessException e) {
            Logs.e(e);
        }
    }

    /**
     * 判断是否VXP
     */
    public static boolean isVirtualXposed(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        String dir = loadPackageParam.appInfo.dataDir + "";
        return dir.contains("io.va.exposed");
    }

    /**
     * 判断是否Hook 模块成功
     */
    public static boolean isHook() {
        return false;
    }

    public static void showVersion(Context activity) {
        try {
            PackageInfo packageInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
            String appName = activity.getResources().getString(packageInfo.applicationInfo.labelRes);
            Toast.makeText(activity, String.format(Locale.CHINA, "%s 启动(%s)", appName, packageInfo.versionName), Toast.LENGTH_LONG).show();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static boolean isSystemApp(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        if (packageName != null) {
            try {
                PackageInfo info = pm.getPackageInfo(packageName, 0);
                return (info != null) && (info.applicationInfo != null) &&
                        ((info.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
            } catch (PackageManager.NameNotFoundException e) {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * 获取当前运行的activity
     */
    public static Activity getTopActivity(Context context) {
        WindowLifecycle windowLifecycle = new WindowLifecycle(context);
        Activity currentActivity = windowLifecycle.getCurrentActivity();
        if (currentActivity != null) {
            return currentActivity;
        }
        try {
            Class activityThreadClass = Class.forName("android.app.ActivityThread");
            Object activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(null);
            Field activitiesField = activityThreadClass.getDeclaredField("mActivities");
            activitiesField.setAccessible(true);
            //16~18 HashMap
            //19~27 ArrayMap
            Map<Object, Object> activities;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                activities = (HashMap<Object, Object>) activitiesField.get(activityThread);
            } else {
                activities = (ArrayMap<Object, Object>) activitiesField.get(activityThread);
            }
            if (activities.size() < 1) {
                return null;
            }
            for (Object activityRecord : activities.values()) {
                Class activityRecordClass = activityRecord.getClass();
                Field pausedField = activityRecordClass.getDeclaredField("paused");
                pausedField.setAccessible(true);
                if (!pausedField.getBoolean(activityRecord)) {
                    Field activityField = activityRecordClass.getDeclaredField("activity");
                    activityField.setAccessible(true);
                    Activity activity = (Activity) activityField.get(activityRecord);
                    return activity;
                }
            }
        } catch (Exception e) {
            Logs.e(e);
        }
        return null;
    }

    /**
     * 监听Application中Activity生命周期，获取顶部Activity
     */
    private static class WindowLifecycle implements Application.ActivityLifecycleCallbacks, Closeable {
        private Activity currentActivity;

        public WindowLifecycle(Context context) {
            if (context == null) {
                return;
            }
            if (context instanceof Application) {
                ((Application) context).registerActivityLifecycleCallbacks(this);
            } else {
                ((Application) context.getApplicationContext()).registerActivityLifecycleCallbacks(this);
            }
        }

        /**
         * 获取当前运行的activity
         */
        Activity getCurrentActivity() {
            return this.currentActivity;
        }

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

        }

        @Override
        public void onActivityStarted(Activity activity) {

        }

        @Override
        public void onActivityResumed(Activity activity) {
            this.currentActivity = activity;
        }

        @Override
        public void onActivityPaused(Activity activity) {
            this.currentActivity = null;
        }

        @Override
        public void onActivityStopped(Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {

        }


        @Override
        public void close() throws IOException {
            if (currentActivity != null) {
                currentActivity = null;
            }
        }

        /**
         * 销毁资源
         */
        public void destroyed() {
            if (currentActivity != null) {
                currentActivity = null;
            }
        }
    }
}
