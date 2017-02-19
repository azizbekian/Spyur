package com.incentive.yellowpages.ui;

import com.facebook.stetho.Stetho;
import com.incentive.yellowpages.ui.base.BaseApplication;

public class Application extends BaseApplication {

    @Override
    public void onCreate() {
        super.onCreate();

        Stetho.initializeWithDefaults(this);
    }
}
