package com.incentive.yellowpages.ui.detail.maps

import android.app.Activity
import com.incentive.yellowpages.ui.base.BaseContract

interface MapsView : BaseContract.View {

    fun initApiClient()

    fun initMap()

    fun showMessage(msg: String)

    fun shouldAskPermission(permissionArr: Array<String>): Boolean

    fun askForPermission(permissionArr: Array<String>)

    fun showMissingPermissionError()

    fun fetchActivity() : Activity

}