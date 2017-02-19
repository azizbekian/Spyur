package com.incentive.yellowpages.injection.component

import android.content.Context
import com.incentive.yellowpages.injection.ApplicationContext
import com.incentive.yellowpages.injection.module.AppModule
import com.incentive.yellowpages.injection.module.BuildTypeModule
import com.incentive.yellowpages.misc.ImageLoader
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(AppModule::class, BuildTypeModule::class))
interface AppComponent {

    @ApplicationContext fun context(): Context

    fun imageLoader(): ImageLoader

    fun configPersistentComponent(): ConfigPersistentComponent

}
