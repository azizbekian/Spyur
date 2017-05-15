package com.incentive.yellowpages.injection.module;

import android.content.Context;
import android.support.v4.app.FragmentActivity;

import com.incentive.yellowpages.injection.ActivityContext;

import dagger.Module;
import dagger.Provides;

@Module
public class ActivityModule {

    private FragmentActivity activity;

    public ActivityModule(FragmentActivity activity) {
        this.activity = activity;
    }

    @Provides
    FragmentActivity provideActivity() {
        return activity;
    }

    @Provides
    @ActivityContext
    Context providesContext() {
        return activity;
    }

}
