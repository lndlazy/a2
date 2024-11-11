package com.pi.connectraspberry.util;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * isMainThread            : 判断当前是否主线程
 * getMainHandler          : 获取主线程 Handler
 * runOnUiThread           : 运行在主线程
 * runOnUiThreadDelayed    : 延时运行在主线程
 * getFixedPool            : 获取固定线程池
 * getSinglePool           : 获取单线程池
 * getCachedPool           : 获取缓冲线程池
 * getIoPool               : 获取 IO 线程池
 * getCpuPool              : 获取 CPU 线程池
 * executeByFixed          : 在固定线程池执行任务
 * executeByFixedWithDelay : 在固定线程池延时执行任务
 * executeByFixedAtFixRate : 在固定线程池按固定频率执行任务
 * executeBySingle         : 在单线程池执行任务
 * executeBySingleWithDelay: 在单线程池延时执行任务
 * executeBySingleAtFixRate: 在单线程池按固定频率执行任务
 * executeByCached         : 在缓冲线程池执行任务
 * executeByCachedWithDelay: 在缓冲线程池延时执行任务
 * executeByCachedAtFixRate: 在缓冲线程池按固定频率执行任务
 * executeByIo             : 在 IO 线程池执行任务
 * executeByIoWithDelay    : 在 IO 线程池延时执行任务
 * executeByIoAtFixRate    : 在 IO 线程池按固定频率执行任务
 * executeByCpu            : 在 CPU 线程池执行任务
 * executeByCpuWithDelay   : 在 CPU 线程池延时执行任务
 * executeByCpuAtFixRate   : 在 CPU 线程池按固定频率执行任务
 * executeByCustom         : 在自定义线程池执行任务
 * executeByCustomWithDelay: 在自定义线程池延时执行任务
 * executeByCustomAtFixRate: 在自定义线程池按固定频率执行任务
 * cancel                  : 取消任务的执行
 * setDeliver              : 设置任务结束后交付的线程
 */
public class ThreadUtil {

    /**
     * 多线程并行线程池
     */
    private static ExecutorService parallelExecutor = Executors.newFixedThreadPool(3);
    /**
     * 单线程串行线程池
     */
    private static ExecutorService serialExecutor = Executors.newSingleThreadExecutor();

    /**
     * 带延迟执行功能的多线程并行线程池
     */
    private static ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2);

    private static Handler mainHandler = null;


    private ThreadUtil() {

    }

    public static ExecutorService getParallelExecutor() {
        return parallelExecutor;
    }

    public static ExecutorService getSerialExecutor() {
        return serialExecutor;
    }

    public static ScheduledExecutorService getScheduledExecutorService() {
        return scheduledExecutorService;
    }


    public static ScheduledFuture<?> parallelExecuteDelayed(long delayMS, Runnable runnable) {
        if (runnable == null) return null;
        ScheduledFuture<?> future = null;
        try {
            future = scheduledExecutorService.schedule(
                    new Runnable() {
                        @Override
                        public void run() {
                            try {
                                runnable.run();
                            } catch (Exception e) {

                            }
                        }
                    }
                    , delayMS, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            future = null;
        }
        return future;
    }

    public static Future<?> parallelExecute(Runnable runnable) {
        if (runnable == null) return null;
        Future<?> future = null;
        try {
            future = parallelExecutor.submit(
                    new Runnable() {
                        @Override
                        public void run() {
                            try {
                                runnable.run();
                            } catch (Exception e) {
                            }
                        }
                    }
            );
        } catch (Exception e) {
            future = null;
        }
        return future;
    }

    public static Future<?> serialExecute(Runnable runnable) {
        if (runnable == null) return null;
        Future<?> future = null;
        try {
            future = serialExecutor.submit(
                    new Runnable() {
                        @Override
                        public void run() {
                            try {
                                runnable.run();
                            } catch (Exception e) {

                            }
                        }
                    });
        } catch (Exception e) {
            future = null;
        }
        return future;
    }


    public static boolean isMainThread() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }


    public static void runOnUIThread(Runnable runnable) {
        if (runnable == null) return;
        try {
            getMainHandler().post(new Runnable() {
                @Override
                public void run() {
                    try {
                        runnable.run();
                    } catch (Exception e) {
                    }
                }
            });
        } catch (Exception e) {

        }
    }

    public static void runOnUIThreadDelayed(Runnable runnable, long delayMillis) {
        if (runnable == null) return;
        try {
            getMainHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        runnable.run();
                    } catch (Exception e) {
                    }
                }
            }, Math.max(0, delayMillis));
        } catch (Exception e) {

        }
    }


    public synchronized static Handler getMainHandler() {
        if (mainHandler == null) {
            mainHandler = new Handler(Looper.getMainLooper());
        }
        return mainHandler;
    }

    //异步转同步方法，外层异步方法调用go，让函数往下运行
    public static void syncRun(SyncBlock syncBlock) {
        Lock lock = new ReentrantLock();
        Condition condition = lock.newCondition();
        AtomicBoolean shouldGo = new AtomicBoolean(false);


        try {
            lock.lock();
        } catch (Exception e) {

        }

        try {
            syncBlock.run(new SyncSignal() {
                @Override
                public void go() {
                    try {
                        lock.lock();
                    } catch (Exception e) {

                    }
                    shouldGo.getAndSet(true);
                    try {
                        condition.signalAll();
                    } catch (Exception e) {

                    }
                    try {
                        lock.unlock();
                    } catch (Exception e) {
                    }
                }
            });
        } catch (Exception e) {
            lock.unlock();
            return;
        }


        if (!shouldGo.get()) {
            try {
                condition.await();
            } catch (Exception e) {

            }
        }
        try {
            lock.unlock();
        } catch (Exception e) {
        }

    }


    public interface SyncBlock {
        void run(SyncSignal syncSignal);
    }

    public interface SyncSignal {
        void go();
    }


}
