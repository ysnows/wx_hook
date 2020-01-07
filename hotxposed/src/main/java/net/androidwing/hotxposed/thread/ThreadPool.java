package net.androidwing.hotxposed.thread;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池工具
 *
 * @author z.houbin
 */
public class ThreadPool {
    private static ThreadPoolExecutor executor;
    private static ScheduledExecutorService scheduledExecutorService;

    private static void init() {
        if (executor == null) {
            executor = new ThreadPoolExecutor(4, 10, 5000, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10), new ThreadPoolExecutor.DiscardPolicy());
        }
        if (scheduledExecutorService == null) {
            scheduledExecutorService = Executors.newScheduledThreadPool(5);
        }
    }

    public static void post(Runnable runnable) {
        init();
        executor.submit(runnable);
    }

    public static void postDelay(Runnable runnable, long delay) {
        init();
        scheduledExecutorService.schedule(runnable, delay, TimeUnit.MILLISECONDS);
    }
}
