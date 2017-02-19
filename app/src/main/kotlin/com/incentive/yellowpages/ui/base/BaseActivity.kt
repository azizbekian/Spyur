package com.incentive.yellowpages.ui.base

import android.os.Bundle
import android.support.v4.util.ArrayMap
import android.support.v7.app.AppCompatActivity
import com.incentive.yellowpages.injection.component.ActivityComponent
import com.incentive.yellowpages.injection.component.ConfigPersistentComponent
import com.incentive.yellowpages.injection.module.ActivityModule
import java.util.concurrent.atomic.AtomicLong

open class BaseActivity : AppCompatActivity() {

    companion object {
        val KEY_ACTIVITY_ID = "activity_id"
        val NEXT_ID = AtomicLong(0)
        val componentMap = ArrayMap<Long, ConfigPersistentComponent>()
    }

    lateinit var activityComponent: ActivityComponent
    var activityId : Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activityId = savedInstanceState?.getLong(KEY_ACTIVITY_ID) ?: NEXT_ID.andIncrement
        val configPersistentComponent: ConfigPersistentComponent
        if (!componentMap.containsKey(activityId)) {
            configPersistentComponent = BaseApplication.appComponent.configPersistentComponent()
            componentMap.put(activityId, configPersistentComponent)
        } else {
            configPersistentComponent = componentMap[activityId]!!
        }
        activityComponent = configPersistentComponent.activityComponent(ActivityModule(this))
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putLong(KEY_ACTIVITY_ID, activityId)
    }

    override fun onDestroy() {
        if (!isChangingConfigurations) componentMap.remove(activityId)
        super.onDestroy()
    }
}
