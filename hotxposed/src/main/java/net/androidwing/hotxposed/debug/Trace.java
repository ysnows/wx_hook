package net.androidwing.hotxposed.debug;

import net.androidwing.hotxposed.log.Logs;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

/**
 * 类调用最终
 *
 * @author z.houbin
 */
public class Trace {
    /**
     * 追踪类函数调用
     *
     * @param cls          类
     * @param debugListner 函数调用回调
     */
    public static void traceClass(Class cls, DebugListner debugListner) {
        Logs.e("Trace Class " + cls);
        if (cls == null) {
            return;
        }
        List<String> methodNames = new ArrayList<>();
        Method[] declaredMethods = cls.getDeclaredMethods();
        for (Method method : declaredMethods) {
            if (!methodNames.contains(method.getName())) {
                methodNames.add(method.getName());
            }
        }
        declaredMethods = cls.getMethods();
        for (Method method : declaredMethods) {
            if (!methodNames.contains(method.getName())) {
                methodNames.add(method.getName());
            }
        }
        DebugMethod debugMethod = new DebugMethod(debugListner);
        for (String name : methodNames) {
            XposedBridge.hookAllMethods(cls, name, debugMethod);
        }
        XposedBridge.hookAllConstructors(cls, debugMethod);
    }

    private static class DebugMethod extends XC_MethodHook {
        private DebugListner debugListner;

        DebugMethod(DebugListner debugListner) {
            this.debugListner = debugListner;
        }

        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            super.beforeHookedMethod(param);
            if (debugListner != null && debugListner.isDebug(param)) {
                debugListner.onMethodBefore(param);
            }
            if (param.thisObject != null) {
                String clsName = param.thisObject.getClass().getName();
                String methodName = param.method.getName();
                try {
                    Logs.printMethodParam(clsName + "--(" + methodName + ") ", param);
                } catch (Exception e) {
                    //e.printStackTrace();
                }
            } else {
                //静态
                String methodName = param.method.getName();
                try {
                    Logs.printMethodParam("???" + "--(" + methodName + ") ", param);
                } catch (Exception e) {
                    //e.printStackTrace();
                }
            }
        }

        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            super.afterHookedMethod(param);
            if (debugListner != null && debugListner.isDebug(param)) {
                debugListner.onMethodAfter(param);
            }
        }
    }
}
