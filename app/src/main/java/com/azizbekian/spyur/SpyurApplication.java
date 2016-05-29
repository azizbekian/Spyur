package com.azizbekian.spyur;

import android.app.Application;

import com.azizbekian.spyur.injection.AppComponent;
import com.azizbekian.spyur.injection.AppModule;
import com.azizbekian.spyur.injection.DaggerAppComponent;

/**
 * Created by CargoMatrix, Inc. on May 02, 2016.
 *
 * @author Andranik Azizbekian (andranik.azizbekyan@gmail.com)
 */
public class SpyurApplication extends Application {

    private static AppComponent sAppComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        sAppComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .build();
    }

    public static AppComponent getAppComponent() {
        return sAppComponent;
    }

}
