package com.azizbekian.spyur.injection;

import android.os.SystemClock;

import com.facebook.stetho.okhttp3.StethoInterceptor;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Created by Joomag, Inc., on July 29, 2016.
 *
 * @author Andranik Azizbekian (a.azizbekyan@joomag.com)
 */
@Module
public class BuildTypeModule {

    @Provides @Singleton
    public OkHttpClient provideOkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        builder
                .addNetworkInterceptor(new StethoInterceptor())
                .addInterceptor(interceptor)
                .addInterceptor(chain -> {
                    // Simulating network delay
                    SystemClock.sleep(1500);
                    return chain.proceed(chain.request());
                });

        return builder.build();
    }

}
