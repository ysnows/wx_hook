package net.androidwing.hotxposed.log;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

import net.androidwing.hotxposed.database.SqliteHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Locale;

import de.robv.android.xposed.XC_MethodHook;

/**
 * 日志工具
 */
public class Logs {
    public static String TAG = "Xposed.Lib";

    public static void init(String t) {
        TAG = t + " ";
    }

    public static void i(String text) {
        Log.i(TAG, text);
    }

    public static void i(String tag, String text) {
        if (tag == null) {
            tag = TAG;
        }
        Log.i(tag, text);
    }

    public static void e(String text) {
        Log.e(TAG, text);
        //Config.writeLog(text);
    }

    public static void e(String tag, String text) {
        Log.e(TAG + " - " + tag, text);
        //Config.writeLog(tag + ": " + text);
    }

    public static void e(Object cls, String log) {
        Logs.e(cls.getClass().getName(), log);
    }

    public static void e(Class cls, String log) {
        Logs.e(cls.getName(), log);
    }

    /**
     * 打印异常信息
     *
     * @param e 异常
     */
    public static void e(Throwable e) {
        e(TAG, Log.getStackTraceString(e));
    }

    public static void e(String tag, Throwable e) {
        e(TAG + " - " + tag, Log.getStackTraceString(e));
    }

    /**
     * 打印函数参数
     *
     * @param tag   标签
     * @param param 参数
     */
    public static void printMethodParam(String tag, XC_MethodHook.MethodHookParam param) {
        StringBuilder builder = new StringBuilder(tag);
        builder.append(" ");
        try {
            Member method = param.method;
            method.getName();

            Object[] params = param.args;
            for (int i = 0; i < params.length; i++) {
                builder.append("p").append(i);
                builder.append(":");
                Object p = params[i];
                if (p == null) {
                    builder.append("null");
                } else {
                    builder.append("(");
                    builder.append(p.getClass().getName());
                    builder.append(")");
                    builder.append(p.toString());
                }
                builder.append(",");
            }
        } catch (Exception e) {
            e(e);
        }
        Logs.e(builder.toString());
    }

    /**
     * 打印成员
     *
     * @param obj 对象
     */
    public static void printField(Object obj) {
        StringBuilder builder = new StringBuilder();
        try {
            Class cls = obj.getClass();
            Field[] fields = cls.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                // 对于每个属性，获取属性名
                String varName = field.getName();
                try {
                    boolean access = field.isAccessible();
                    if (!access) {
                        field.setAccessible(true);
                    }

                    //从obj中获取field变量
                    Object o = field.get(obj);
                    builder.append("变量： " + varName + " = " + o);
                    builder.append("\r\n");
                    if (!access) {
                        field.setAccessible(false);
                    }
                } catch (Exception ex) {
                    e(ex);
                }
            }

            //函数
            Method[] methods = cls.getDeclaredMethods();
            for (Method method : methods) {
                // 对于每个属性，获取属性名
                //得到方法的返回值类型的类类型
                builder.append("\r\n");
                Class returnType = method.getReturnType();
                builder.append(returnType.getName());
                builder.append("  ");
                //得到方法的名称
                builder.append(method.getName());
                builder.append("(");
                //获取参数类型--->得到的是参数列表的类型的类类型
                Class[] paramTypes = method.getParameterTypes();
                for (Class class1 : paramTypes) {
                    builder.append(class1.getName());
                    builder.append(",");
                }
                builder.append(")");
            }
            Logs.e(cls.getName() + " method " + builder.toString());
        } catch (Exception e) {
            e(e);
        }
        Logs.e(builder.toString());
    }

    /**
     * 打印当前堆栈信息
     */
    public static void printStackTrace() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        StringBuilder builder = new StringBuilder();
        String lineSeparator = System.getProperty("line.separator");
        for (StackTraceElement trace : stackTrace) {
            if (builder.length() > 0) {
                builder.append(lineSeparator);
            }
            builder.append(java.text.MessageFormat.format("{0}.{1}() {2}"
                    , trace.getClassName()
                    , trace.getMethodName()
                    , trace.getLineNumber()));
        }
        Logs.e("StackTrace \r\n" + builder.toString());
    }

    public static void e(Object tag, Object... log) {
        if (log.length == 1) {
            Logs.e(tag, log[0]);
        } else if (log.length > 1) {
            //多个格式化
            Logs.e(TAG, String.format(Locale.CHINA, tag.toString(), log));
        }
    }

    private static void e(Object tag, Object log) {
        Logs.e(getTag(tag), getLog(log));
    }

    private static String getLog(Object log) {
        StringBuilder builder = new StringBuilder();
        if (log instanceof Bundle) {
            builder.append(getBundleLog((Bundle) log));
        } else if (log instanceof Cursor) {
            builder.append(getCursorLog((Cursor) log));
        } else if (log.getClass().isArray()) {
            builder.append(getArrayLog((Object[]) log));
        } else {
            builder.append(log.toString());
        }
        return builder.toString();
    }

    private static String getArrayLog(Object[] arr) {
        StringBuilder builder = new StringBuilder();
        if (arr != null) {
            for (int i = 0; i < arr.length; i++) {
                builder.append("[");
                builder.append(i);
                builder.append("]");
                builder.append(": ");
                builder.append(arr[i]);
                builder.append("   ");
            }
        }
        return builder.toString();
    }

    private static String getBundleLog(Bundle bundle) {
        StringBuilder builder = new StringBuilder();
        for (String key : bundle.keySet()) {
            Object v = bundle.get(key);
            if (v == null) {
                v = "";
            }
            builder.append(key);
            builder.append(":");
            builder.append(v.toString());
            builder.append("--");
        }
        return builder.toString();
    }

    private static String getCursorLog(Cursor cursor) {
        StringBuilder builder = new StringBuilder();
        try {
            builder.append(SqliteHelper.dumpCursor(cursor));
        } catch (Exception e) {
            Logs.e(e);
        }
        return builder.toString();
    }

    private static String getTag(Object tag) {
        String t = TAG;
        if (tag == null) {
            t += "";
        } else if (tag instanceof String) {
            t += tag.toString();
        } else if (tag instanceof Class) {
            t += ((Class) tag).getName();
        } else {
            t += tag.getClass().getSimpleName();
        }
        return t;
    }
}
