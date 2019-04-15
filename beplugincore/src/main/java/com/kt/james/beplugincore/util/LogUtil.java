package com.kt.james.beplugincore.util;

import android.util.Log;

/**
 * author: James
 * 2019/4/10 22:59
 * version: 1.0
 */
public class LogUtil {

    private static final String TAG = "BepluginLog";

    private static LogHandler mLogHandler = new LogHandler() {
        @Override
        public void publish(String tag, int level, String message) {
            Log.println(level, tag, message);
        }
    };

    public static void w(Object... param) {
        StringBuilder sb = new StringBuilder();
        for (Object obj : param) {
            sb.append(obj).append(" ");
        }
        w(sb.toString());
    }

    public static void d(Object... param) {
        StringBuilder sb = new StringBuilder();
        for (Object obj : param) {
            sb.append(obj).append(" ");
        }
        d(sb.toString());
    }

    public static void e(Object... param) {
        StringBuilder sb = new StringBuilder();
        for (Object obj : param) {
            sb.append(obj).append(" ");
        }
        e(sb.toString());
    }

    public static void w(String msg) {
        Log.e(TAG, msg);
    }

    public static void d(String msg) {
        Log.d(TAG, msg);
    }

    public static void e(String msg) {
        Log.e(TAG, msg);
    }

    public static void printException(String msg, Throwable e) {
        mLogHandler.publish("Log_StackTrace", Log.ERROR,
                msg + "\n" + Log.getStackTraceString(e));
    }

    public interface LogHandler {
        void publish(String tag, int level, String message);
    }

}
