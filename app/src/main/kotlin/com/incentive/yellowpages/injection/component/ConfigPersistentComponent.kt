package com.incentive.yellowpages.injection.component

import com.incentive.yellowpages.injection.ConfigPersistent
import com.incentive.yellowpages.injection.module.ActivityModule
import dagger.Subcomponent

@ConfigPersistent
@Subcomponent
interface ConfigPersistentComponent {

    fun activityComponent(activityModule: ActivityModule): ActivityComponent
}
