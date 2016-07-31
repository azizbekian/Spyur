package com.azizbekian.spyur;

import android.content.Context;
import android.support.multidex.MultiDexApplication;

import com.azizbekian.spyur.injection.AppComponent;
import com.azizbekian.spyur.injection.AppModule;
import com.azizbekian.spyur.injection.DaggerAppComponent;

/**
 * Created on July 29, 2016.
 *
 * @author Andranik Azizbekian (andranik.azizbekyan@gmail.com)
 */
public class BaseApplication extends MultiDexApplication {

    private static AppComponent sAppComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        sAppComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .build();
    }

    public static AppComponent getComponent() {
        return sAppComponent;
    }

    public static Context getContext() {
        return sAppComponent.getApplicationContext();
    }
}
