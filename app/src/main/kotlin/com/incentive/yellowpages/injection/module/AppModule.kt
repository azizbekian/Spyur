package com.incentive.yellowpages.injection.module

import android.content.Context
import com.incentive.yellowpages.data.remote.ApiContract
import com.incentive.yellowpages.injection.ApplicationContext
import com.incentive.yellowpages.misc.converter.ListingConverter
import com.incentive.yellowpages.misc.converter.SearchConverter
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class AppModule(private val context: Context) {

    companion object {
        val DURATION_CONNECTION_TIMEOUT = 10
        val DURATION_WRITE_TIMEOUT = 10
        val DURATION_READ_TIMEOUT = 30
    }

    @Provides
    @Singleton
    @ApplicationContext
    fun provideAppContext(): Context {
        return context
    }

    @Provides
    @Singleton
    fun applyTimeouts(okHttpBuilder: OkHttpClient.Builder): OkHttpClient {
        return okHttpBuilder.connectTimeout(DURATION_CONNECTION_TIMEOUT.toLong(), TimeUnit.SECONDS)
                .writeTimeout(DURATION_WRITE_TIMEOUT.toLong(), TimeUnit.SECONDS)
                .readTimeout(DURATION_READ_TIMEOUT.toLong(), TimeUnit.SECONDS)
                .build()
    }

    @Provides
    @Singleton
    fun providesSearchApi(okHttp: OkHttpClient): ApiContract.SearchApi {
        return Retrofit.Builder()
                .baseUrl(ApiContract.BASE)
                .client(okHttp)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(SearchConverter.Factory())
                .build().create(ApiContract.SearchApi::class.java)
    }

    @Provides
    @Singleton
    fun providesListingApi(okHttp: OkHttpClient): ApiContract.ListingApi {
        return Retrofit.Builder()
                .baseUrl(ApiContract.BASE)
                .client(okHttp)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(ListingConverter.Factory())
                .build().create(ApiContract.ListingApi::class.java)
    }

}
