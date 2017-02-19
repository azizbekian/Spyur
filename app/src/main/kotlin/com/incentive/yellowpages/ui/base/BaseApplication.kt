package com.incentive.yellowpages.ui.base

import android.content.Context
import android.support.multidex.MultiDexApplication
import com.bumptech.glide.request.target.ViewTarget
import com.incentive.yellowpages.BuildConfig
import com.incentive.yellowpages.R
import com.incentive.yellowpages.injection.component.AppComponent
import com.incentive.yellowpages.injection.component.DaggerAppComponent
import com.incentive.yellowpages.injection.module.AppModule
import timber.log.Timber

open class BaseApplication : MultiDexApplication() {

    companion object {
        @JvmStatic lateinit var appComponent: AppComponent
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()

        // workaround for Glide issue - https://goo.gl/sLdXQx
        ViewTarget.setTagId(R.id.glide_tag)

        appComponent = DaggerAppComponent.builder()
                .appModule(AppModule(this))
                .build()

        context = this
        plantTimber()
    }

    private fun plantTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

}
