package com.incentive.yellowpages.ui.detail.maps

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.support.annotation.NonNull
import android.widget.Toast
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.incentive.yellowpages.R
import com.incentive.yellowpages.data.model.ListingResponse
import com.incentive.yellowpages.misc.hasVersionM
import com.incentive.yellowpages.ui.base.BaseActivity
import com.incentive.yellowpages.ui.detail.maps.MapsPresenter.Companion.LOCATION_PERMISSION_REQUEST_CODE
import com.incentive.yellowpages.utils.PermissionUtils
import java.util.*
import javax.inject.Inject

class MapsActivity : BaseActivity(), MapsView {

    companion object {

        fun launch(context: Context, @NonNull listingResponse: ListingResponse) {
            val intent = Intent(context, MapsActivity::class.java)
            intent.putParcelableArrayListExtra(
                    MapsPresenter.KEY_COORDINATES,
                    ArrayList<Parcelable>(listingResponse.contactInfos))
            context.startActivity(intent)
        }

        fun launch(activity: Activity, latLng: LatLng?) {
            val intent = Intent(activity, MapsActivity::class.java)
            intent.putExtra(MapsPresenter.KEY_SINGLE_COORDINATE, latLng)
            activity.startActivity(intent)
        }
    }

    @Inject lateinit var presenter: MapsPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        MapsInitializer.initialize(applicationContext)
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        setContentView(R.layout.activity_map)
        activityComponent.injectActivityComponent(this)

        presenter.create(this, intent = intent)
    }

    override fun onStart() {
        super.onStart()
        presenter.start()
    }

    override fun onResume() {
        super.onResume()
        presenter.resume()
    }

    override fun onPause() {
        presenter.pause()
        if (isFinishing) {
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }
        super.onPause()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (!presenter.permissionResult(requestCode, permissions, grantResults)) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun initApiClient() {
        presenter.setupApiClient(GoogleApiClient.Builder(this))
    }

    override fun initMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync({ presenter.onMapReady(it) })
    }

    override fun showMessage(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("NewApi")
    override fun shouldAskPermission(permissionArr: Array<String>): Boolean = hasVersionM()
            && !PermissionUtils.isPermissionsGranted(this, permissionArr)

    override fun askForPermission(permissionArr: Array<String>) {
        PermissionUtils.requestPermissions(this, LOCATION_PERMISSION_REQUEST_CODE, permissionArr)
    }

    override fun showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog.newInstance(false)
                .show(supportFragmentManager, "dialog")
    }

    override fun fetchActivity(): Activity = this

}
