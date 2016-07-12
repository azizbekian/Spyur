package com.azizbekian.spyur.utils;

import com.azizbekian.spyur.BuildConfig;

/**
 * Created by Joomag, Inc., on June 16, 2016.
 *
 * @author Andranik Azizbekian (a.azizbekyan@joomag.com)
 */
public class ExceptionUtils {

    private ExceptionUtils() {
        throw new RuntimeException("Unable to instantiate class " + getClass().getCanonicalName());
    }

    /**
     * Called, when wrong input parameters are provided to the {@code clazz}.
     * <p>
     * If in debug mode - throws {@link RuntimeException}. Otherwise prints in log.
     */
    public static void throwWrongInput(Class clazz) {
        String msg = "Wrong input to class " + clazz.getSimpleName();
        if (BuildConfig.DEBUG) throw new RuntimeException(msg);
        else LogUtils.e(msg);
    }

}
