package com.incentive.yellowpages.injection.component

import com.incentive.yellowpages.injection.PerActivity
import com.incentive.yellowpages.injection.module.ActivityModule
import com.incentive.yellowpages.ui.detail.DetailActivity
import com.incentive.yellowpages.ui.detail.maps.MapsActivity
import com.incentive.yellowpages.ui.main.MainActivity
import dagger.Subcomponent

@PerActivity
@Subcomponent(modules = arrayOf(ActivityModule::class))
interface ActivityComponent {

    fun injectActivityComponent(mainActivity: MainActivity)
    fun injectActivityComponent(detailActivity: DetailActivity)
    fun injectActivityComponent(mapsActivity: MapsActivity)

}
