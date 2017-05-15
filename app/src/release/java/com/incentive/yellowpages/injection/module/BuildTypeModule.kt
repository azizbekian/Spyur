package com.incentive.yellowpages.injection.module

import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
class BuildTypeModule {

    @Provides
    @Singleton
    internal fun provideOkHttpClient(): OkHttpClient.Builder {
        return OkHttpClient.Builder()
    }

}
