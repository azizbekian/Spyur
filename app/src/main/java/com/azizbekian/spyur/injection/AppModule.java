package com.azizbekian.spyur.injection;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import javax.inject.Singleton;

import com.azizbekian.spyur.BuildConfig;
import com.azizbekian.spyur.manager.SpyurManager;
import com.azizbekian.spyur.converter.ListingConverter;
import com.azizbekian.spyur.converter.SearchConverter;
import com.azizbekian.spyur.rest.SpyurApi;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;

/**
 * Created on May 09, 2016.
 *
 * @author Andranik Azizbekian (andranik.azizbekyan@gmail.com)
 */

@Module
public class AppModule {

    private Context context;

    public AppModule(Context context) {
        this.context = context;
    }

    @Provides
    @Singleton
    public Context provideContext() {
        return context;
    }

    @Provides
    @Singleton
    public OkHttpClient provideOkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
            builder.addInterceptor(interceptor).addInterceptor(chain -> {
                // Simulating network delay
                // SystemClock.sleep(3000);
                return chain.proceed(chain.request());
            });
        }

        return builder.build();
    }

    @Provides
    @Singleton
    public SpyurApi.SearchApi provideSearchApi(OkHttpClient client) {

        return new Retrofit.Builder()
                .baseUrl(SpyurApi.ENDPOINT)
                .client(client)
                .addConverterFactory(new SearchConverter.Factory())
                .build()
                .create(SpyurApi.SearchApi.class);
    }

    @Provides
    @Singleton
    public SpyurApi.ListingApi provideListingApi(OkHttpClient client) {

        return new Retrofit.Builder()
                .baseUrl(SpyurApi.ENDPOINT)
                .client(client)
                .addConverterFactory(new ListingConverter.Factory())
                .build()
                .create(SpyurApi.ListingApi.class);
    }

    @Provides
    @Singleton
    public SpyurManager provideSpyurManager(SpyurApi.SearchApi searchApi, SpyurApi.ListingApi
            listingApi) {
        return new SpyurManager(searchApi, listingApi);
    }

    @Provides
    @Singleton
    public RequestManager provideGlide(Context context) {
        return Glide.with(context);
    }
}