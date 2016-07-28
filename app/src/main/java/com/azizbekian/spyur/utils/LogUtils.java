package com.azizbekian.spyur.utils;

import android.support.design.BuildConfig;
import android.util.Log;

/**
 * Created on April 02, 2016.
 *
 * @author Andranik Azizbekian (andranik.azizbekyan@gmail.com)
 */
public class LogUtils {

    private static final String PREFIX = "vvv";

    private static final boolean IS_DEBUGGABLE = BuildConfig.DEBUG;

    private LogUtils() {
        throw new RuntimeException("Unable to instantiate class " + getClass().getCanonicalName());
    }

    public static void wtf(String msg) {
        if (IS_DEBUGGABLE) Log.wtf(PREFIX, msg);
    }

    public static void i(String msg) {
        if (IS_DEBUGGABLE) Log.i(PREFIX, msg);
    }

    public static void e(String msg) {
        if (IS_DEBUGGABLE) Log.wtf(PREFIX, msg);
    }

    public static void wtf(int msg) {
        wtf("" + msg);
    }

    public static void i(int msg) {
        i("" + msg);
    }

    public static void e(int msg) {
        e("" + msg);
    }

    public static void wtf(boolean msg) {
        wtf("" + msg);
    }

    public static void i(boolean msg) {
        i("" + msg);
    }

    public static void e(boolean msg) {
        e("" + msg);
    }

    public static void wtf(float msg) {
        wtf("" + msg);
    }

    public static void i(float msg) {
        i("" + msg);
    }

    public static void e(float msg) {
        e("" + msg);
    }
}
