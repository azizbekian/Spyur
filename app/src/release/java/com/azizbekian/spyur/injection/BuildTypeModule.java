package com.azizbekian.spyur.injection;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;

/**
 * Created by Joomag, Inc., on July 29, 2016.
 *
 * @author Andranik Azizbekian (a.azizbekyan@joomag.com)
 */
@Module
public class BuildTypeModule {

    @Provides @Singleton
    public OkHttpClient provideOkHttpClient() {
        return new OkHttpClient.Builder().build();
    }
}
