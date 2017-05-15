package com.incentive.yellowpages.ui.detail.maps

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import android.support.annotation.Nullable
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.incentive.yellowpages.data.model.ListingResponse.ContactInfo
import com.incentive.yellowpages.injection.ApplicationContext
import com.incentive.yellowpages.injection.ConfigPersistent
import com.incentive.yellowpages.ui.base.BaseContract
import com.incentive.yellowpages.utils.PermissionUtils
import java.util.*
import javax.inject.Inject


@ConfigPersistent
class MapsPresenter @Inject constructor(@ApplicationContext val appContext: Context) : BaseContract.BasePresenter<MapsView>(),
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    companion object {
        val KEY_COORDINATES = "key_coordinates"
        val KEY_SINGLE_COORDINATE = "key_single_coordinate"
        val LOCATION_PERMISSION_REQUEST_CODE = 135

        private val PLAY_SERVICES_RESOLUTION_REQUEST = 9000
        private val LOCATION_YEREVAN = LatLng(40.1856275, 44.4984549)
        private val DURATION_CAMERA_ANIM = 2000
        private val PADDING_BOUNDS = 130
        private val NEEDED_PERMISSION = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION)
        private val UPDATE_INTERVAL: Long = 10 * 1000  /* 10 secs */
        private val FASTEST_INTERVAL: Long = 2000 /* 2 sec */
    }

    var googleMap: GoogleMap? = null
    var googleApiClient: GoogleApiClient? = null
    private var coordinatesList: List<LatLng>? = null
    private val markerOptions = MarkerOptions()
    private var permissionDenied: Boolean = false
    private var cameraUpdate: CameraUpdate? = null
    private val locationRequest: LocationRequest
    private var bitmapDescriptor: BitmapDescriptor? = null
    /**
     * Tracks the case, when user's location is being received when the camera has already been
     * positioned, so another [CameraUpdate] is being created to include user's location in
     * rectangle with all other locations.
     */
    private var hasOnceReceivedLocation: Boolean = false
    /**
     * Indicates, whether the System's permission dialog is currently opened. Is used to keep track
     * and do not ask for another permission after orientation change.
     */
    private var isPermissionDialogOpened = false
    private var hasShownGooglePlayServicesAbsentDialog = false
    private var googleDialog: Dialog? = null

    init {
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL)
    }

    override fun create(view: MapsView, savedInstanceState: Bundle?, intent: Intent?,
                        arguments: Bundle?, isPortrait: Boolean) {
        super.create(view, savedInstanceState, intent, arguments, isPortrait)

        initBitmapDescriptor()

        intent?.apply {
            if (hasExtra(KEY_COORDINATES)) {
                val contactInfoList = getParcelableArrayListExtra<ContactInfo>(KEY_COORDINATES)
                coordinatesList = contactInfoList.mapNotNull { it.loc }.toList()
            } else if (hasExtra(KEY_SINGLE_COORDINATE)) {
                coordinatesList = ArrayList(1)
                val p = extras.getParcelable<Parcelable>(KEY_SINGLE_COORDINATE)
                if (p != null && p is LatLng) (coordinatesList as ArrayList<LatLng>).add(p)
            }
        }

        view.apply {
            initMap()
            if (!shouldAskPermission(NEEDED_PERMISSION)) initApiClient()
        }

        createBounds(null)
    }

    override fun start() {
        googleApiClient?.connect()
    }

    override fun resume() {
        super.resume()
        if (permissionDenied) {
            view?.showMissingPermissionError()
            permissionDenied = false
        } else if (null != googleApiClient && googleApiClient!!.isConnected) {
            startLocationUpdates()
        }
    }

    override fun pause() {
        if (null != googleApiClient && googleApiClient!!.isConnected) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this)
            googleApiClient?.disconnect()
        }
        googleDialog?.dismiss()
    }

    override fun permissionResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray): Boolean {
        isPermissionDialogOpened = false
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) return false

        if (PermissionUtils.isPermissionsGranted(grantResults)) {
            view?.initApiClient()
            googleApiClient?.connect()
            enableMyLocation()
        } else permissionDenied = true
        return true
    }

    fun setupApiClient(googleApiClientBuilder: GoogleApiClient.Builder) {
        googleApiClient = googleApiClientBuilder
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build()
    }

    fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.apply {
            moveCamera(CameraUpdateFactory.newLatLng(LOCATION_YEREVAN))
            uiSettings.isMapToolbarEnabled = false
            setPadding(0, 100, 0, 0)
        }
        enableMyLocation()

        if (null != coordinatesList) {
            coordinatesList!!.forEach {
                googleMap?.addMarker(markerOptions
                        .position(it)
                        .icon(bitmapDescriptor)
                        .flat(true))
            }

            Handler().postDelayed({
                if (coordinatesList!!.size == 1) {
                    googleMap?.animateCamera(CameraUpdateFactory
                            .newLatLngZoom(coordinatesList!!.first(), 16.0f), DURATION_CAMERA_ANIM, null)
                } else if (null != cameraUpdate) googleMap?.animateCamera(cameraUpdate,
                        DURATION_CAMERA_ANIM, null)
            }, 500)
        }
    }

    /**
     * Creates [LatLngBounds], which zooms to a rectangle, in which all the locations are
     * being displayed. Applies that [LatLngBounds] to [CameraUpdate].
     */
    private fun createBounds(@Nullable userLocation: Location?) {
        val builder = LatLngBounds.Builder()
        if (null != coordinatesList && coordinatesList!!.isNotEmpty()) {
            coordinatesList?.forEach { builder.include(it) }
            userLocation?.apply { builder.include(LatLng(latitude, longitude)) }
            val bounds = builder.build()
            cameraUpdate = createCameraUpdateFactory(bounds)
        }
    }

    fun enableMyLocation() {
        val shouldAskPermission = view?.shouldAskPermission(NEEDED_PERMISSION)!!
        if (!shouldAskPermission) {
            googleMap?.isMyLocationEnabled = true
        } else if (!isPermissionDialogOpened) {
            isPermissionDialogOpened = true
            view?.askForPermission(NEEDED_PERMISSION)
        }
    }

    override fun onConnected(p0: Bundle?) {
        val currentLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient)
        if (currentLocation != null) onLocationChanged(currentLocation)
        startLocationUpdates()
    }

    fun startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient,
                locationRequest, this)
    }

    override fun onConnectionSuspended(cause: Int) {
        if (cause == CAUSE_SERVICE_DISCONNECTED) {
            view?.showMessage("Disconnected. Please re-connect.")
        } else if (cause == CAUSE_NETWORK_LOST) {
            view?.showMessage("Network lost. Please re-connect.")
        }
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        view?.showMessage("Connection failed.")
    }

    override fun onLocationChanged(location: Location?) {
        if (!hasOnceReceivedLocation) {
            hasOnceReceivedLocation = true
            createBounds(location)
            googleMap?.animateCamera(cameraUpdate, DURATION_CAMERA_ANIM, null)
        }
    }

    private fun initBitmapDescriptor() {
        if (verifyGooglePlayServicesInstalled()) {
            bitmapDescriptor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
        }
    }

    private fun createCameraUpdateFactory(bounds: LatLngBounds): CameraUpdate? {
        if (verifyGooglePlayServicesInstalled()) {
            return CameraUpdateFactory.newLatLngBounds(bounds, PADDING_BOUNDS)
        }
        return null
    }

    private fun verifyGooglePlayServicesInstalled(): Boolean {
        val googleAPI = GoogleApiAvailability.getInstance()
        val result = googleAPI.isGooglePlayServicesAvailable(appContext)
        if (result != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(result) && !hasShownGooglePlayServicesAbsentDialog) {
                hasShownGooglePlayServicesAbsentDialog = true
                googleDialog = googleAPI.getErrorDialog(view?.fetchActivity(), result, PLAY_SERVICES_RESOLUTION_REQUEST)
                googleDialog?.show()
            }
            return false
        }
        return true
    }

}
