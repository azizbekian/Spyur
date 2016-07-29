package com.azizbekian.spyur.injection;

import android.content.Context;

import com.azizbekian.spyur.api.ApiInteractor;
import com.bumptech.glide.RequestManager;

import javax.inject.Singleton;

import com.azizbekian.spyur.activity.ListingActivity;

import dagger.Component;

/**
 * Created on May 09, 2016.
 *
 * @author Andranik Azizbekian (andranik.azizbekyan@gmail.com)
 */

@Singleton
@Component(modules = {AppModule.class, BuildTypeModule.class})
public interface AppComponent {

    /**
     * @return {@link com.azizbekian.spyur.SpyurApplication SpyurApplication} context.
     */
     Context getApplicationContext();

    /**
     * @return Singleton {@link ApiInteractor} instance.
     */
     ApiInteractor getApiInteractor();

    /**
     * @return Singleton {@link RequestManager} instance.
     */
     RequestManager getGlide();

}
