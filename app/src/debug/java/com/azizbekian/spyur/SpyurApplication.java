package com.azizbekian.spyur;

import com.facebook.stetho.Stetho;

/**
 * Created on May 09, 2016.
 *
 * @author Andranik Azizbekian (andranik.azizbekyan@gmail.com)
 */
public class SpyurApplication extends BaseApplication {

    @Override
    public void onCreate() {
        super.onCreate();

        initStetho();
    }

    private void initStetho() {
        Stetho.initializeWithDefaults(this);
    }

}
