package com.azizbekian.spyur.injection;

import android.content.Context;

import com.bumptech.glide.RequestManager;

import javax.inject.Singleton;

import com.azizbekian.spyur.activity.ListingActivity;
import com.azizbekian.spyur.manager.SpyurManager;

import dagger.Component;

/**
 * Created on May 09, 2016.
 *
 * @author Andranik Azizbekian (andranik.azizbekyan@gmail.com)
 */

@Singleton
@Component(modules = {AppModule.class})
public interface AppComponent {

    /**
     * Injects all the fields from object graph.
     */
    void inject(ListingActivity listingActivity);

    /**
     * @return {@link com.azizbekian.spyur.SpyurApplication SpyurApplication} context.
     */
    Context getApplicationContext();

    /**
     * @return Singleton {@link SpyurManager} instance.
     */
    SpyurManager getSpyurManager();

    /**
     * @return Singleton {@link RequestManager} instance.
     */
    RequestManager getGlide();
}
