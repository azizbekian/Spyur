package com.azizbekian.spyur.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import com.azizbekian.spyur.R;
import com.azizbekian.spyur.model.ListingResponse;
import com.azizbekian.spyur.utils.PermissionUtils;

/**
 * Created on May 09, 2016.
 *
 * @author Andranik Azizbekian (andranik.azizbekyan@gmail.com)
 */
public class MapActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnMyLocationChangeListener {

    private static final String KEY_COORDINATES = "key_coordinates";
    private static final String KEY_SINGLE_COORDINATE = "key_single_coordinate";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final LatLng LOCATION_YEREVAN = new LatLng(40.1856275, 44.4984549);
    private static final int DURATION_CAMERA_ANIM = 2000;
    private static final int PADDING_BOUNDS = 130;
    private static final String[] NEEDED_PERMISSION = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};

    public static void launch(Activity activity, @NonNull ListingResponse listingResponse) {
        Intent intent = new Intent(activity, MapActivity.class);
        intent.putParcelableArrayListExtra(KEY_COORDINATES,
                new ArrayList<>(listingResponse.contactInfos));
        activity.startActivity(intent);
    }

    public static void launch(Activity activity, @NonNull LatLng latLng) {
        Intent intent = new Intent(activity, MapActivity.class);
        intent.putExtra(KEY_SINGLE_COORDINATE, latLng);
        activity.startActivity(intent);
    }

    private List<LatLng> mCoordinatesList;
    private CameraUpdate mCameraUpdate;
    private MarkerOptions mMarkerOptions = new MarkerOptions();
    private BitmapDescriptor mBitmapDescriptor;
    private GoogleMap mMap;
    private boolean mPermissionDenied;

    /**
     * Tracks the case, when user's location is being received when the camera has already been
     * positioned, so another {@link CameraUpdate} is being created to include user's location in
     * rectangle with all other locations.
     */
    private boolean mHasOnceReceivedLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        setContentView(R.layout.activity_map);

        initMap();
        mBitmapDescriptor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE);

        Intent intent = getIntent();

        if (intent.hasExtra(KEY_COORDINATES)) {
            List<ListingResponse.ContactInfo> contactInfoList = intent
                    .getParcelableArrayListExtra(KEY_COORDINATES);
            mCoordinatesList = new ArrayList<>(contactInfoList.size());
            for (ListingResponse.ContactInfo c : contactInfoList) {
                if (c.loc != null) mCoordinatesList.add(c.loc);
            }
        } else if (intent.hasExtra(KEY_SINGLE_COORDINATE)) {
            mCoordinatesList = new ArrayList<>(1);
            Parcelable p = intent.getExtras().getParcelable(KEY_SINGLE_COORDINATE);
            if (p instanceof LatLng) {
                mCoordinatesList.add((LatLng) p);
            }
        }
        createBounds(null);
    }

    /**
     * Creates {@link LatLngBounds}, which zooms to a rectangle, in which all the locations are
     * being displated. Applies that {@link LatLngBounds} tp {@link CameraUpdate}.
     */
    private void createBounds(@Nullable Location userLocation) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        if (null != mCoordinatesList) {
            for (LatLng latLng : mCoordinatesList) {
                builder.include(latLng);
            }
            if (null != userLocation) {
                builder.include(new LatLng(userLocation.getLatitude(), userLocation.getLongitude()));
            }
            LatLngBounds bounds = builder.build();
            mCameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, PADDING_BOUNDS);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // if the activity is being finished - animate back
        if (isFinishing()) overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) return;

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            enableMyLocation();
        } else mPermissionDenied = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog.newInstance(false)
                .show(getSupportFragmentManager(), "dialog");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLng(LOCATION_YEREVAN));
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setPadding(0, 100, 0, 0);
        mMap.setOnMyLocationChangeListener(this);
        if (null != mCoordinatesList) {
            for (LatLng latLng : mCoordinatesList) {
                mMap.addMarker(mMarkerOptions
                        .position(latLng)
                        .icon(mBitmapDescriptor)
                        .flat(true));
            }
            new Handler().postDelayed(() -> {
                if (mCoordinatesList.size() == 1) {
                    mMap.animateCamera(CameraUpdateFactory
                            .newLatLngZoom(mCoordinatesList.get(0), 16), DURATION_CAMERA_ANIM, null);
                } else {
                    if (null != mCameraUpdate) {
                        mMap.animateCamera(mCameraUpdate, DURATION_CAMERA_ANIM, null);
                    }
                }
            }, 500);
        }
        enableMyLocation();
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    @SuppressWarnings("MissingPermission")
    public void enableMyLocation() {
        if (!PermissionUtils.isPermissionsGranted(this, NEEDED_PERMISSION)) {
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE, NEEDED_PERMISSION[0], false);
        } else if (mMap != null) {
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onMyLocationChange(Location location) {
        if (!mHasOnceReceivedLocation && null != mMap) {
            mHasOnceReceivedLocation = true;
            createBounds(location);
            mMap.animateCamera(mCameraUpdate, DURATION_CAMERA_ANIM, null);
        }
    }

}
