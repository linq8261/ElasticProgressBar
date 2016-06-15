package com.audienl.elasticprogressbarcore;

import android.os.Handler;
import android.os.Looper;

/**
 * @author audienl@qq.com on 2016/6/14 0014
 */
public class HandlerUtils {

    public static void post(Runnable run) {
        new Handler(Looper.getMainLooper()).post(run);
    }

    public static void postDelayed(Runnable run, long delay_ms) {
        new Handler(Looper.getMainLooper()).postDelayed(run, delay_ms);
    }

    /**
     * 可以传入参数。
     */
    public static void post(ArgsRunnable run) {
        new Handler(Looper.getMainLooper()).post(run);
    }

    /**
     * 可以传入参数。
     */
    public static void postDelayed(ArgsRunnable run, long delay_ms) {
        new Handler(Looper.getMainLooper()).postDelayed(run, delay_ms);
    }

    public static abstract class ArgsRunnable implements Runnable {
        private Object[] args;

        public ArgsRunnable(Object... args) {
            this.args = args;
        }

        @Override
        public void run() {
            run(args);
        }

        public abstract void run(Object... args);
    }
}
