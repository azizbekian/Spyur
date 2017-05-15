package com.incentive.yellowpages.injection.module

import com.facebook.stetho.okhttp3.StethoInterceptor
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import javax.inject.Singleton

@Module
class BuildTypeModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient.Builder {
        val builder = OkHttpClient.Builder()
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BASIC
        builder
                .addNetworkInterceptor(StethoInterceptor())
                .addInterceptor(logging)
                .addInterceptor { chain ->
                    // Simulating network delay
                    // SystemClock.sleep(3000)
                    chain.proceed(chain.request())
                }

        return builder
    }

}
