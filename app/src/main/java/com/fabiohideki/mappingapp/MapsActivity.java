package com.fabiohideki.mappingapp;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.tasks.OnSuccessListener;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, OnSuccessListener<Location> {

    private static final int ERROR_DIALOG_REQUEST = 9001;
    private static String TAG = "Fabio";
    private static final int PERM_REQUEST_CODE = 200;
    private static final String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    //Define fields for Google API Client
    FusedLocationProviderClient mFusedLocationClient;

    LocationRequest locationRequest;
    LocationCallback locationCallback;

    private GoogleMap mMap;

    private static final double
            SEATTLE_LAT = 47.60621,
            SEATTLE_LNG = -122.33207;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (serviceOK()) {
            Toast.makeText(this, "Ready to map!", Toast.LENGTH_SHORT).show();
            setContentView(R.layout.activity_maps);
            initMap();
        } else {
            setContentView(R.layout.activity_maps_blank);
        }

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    Activity#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                Toast.makeText(this, "Need Location permission", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERM_REQUEST_CODE);
                return;
            }
        }
        mMap.setMyLocationEnabled(true);
        mFusedLocationClient.getLastLocation().addOnSuccessListener(this);

        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(3000);
        locationRequest.setFastestInterval(1000);

        locationCallback = new LocationCallback() {

            @Override
            public void onLocationResult(LocationResult locationResult) {

                if (locationResult == null)
                    return;

                Log.d("Fabio", "onLocationResult: entered");

                for (Location location : locationResult.getLocations()) {

                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                    Log.d("Fabio", "onLocationResult: " + location.getLatitude() + " - " + location.getLongitude());

                    CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, 15);

                    mMap.animateCamera(update);
                }

            }
        };

        mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {

            case PERM_REQUEST_CODE:

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onMapReady(mMap);
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(perms, PERM_REQUEST_CODE);
                    }
                }

                break;

        }
    }


    public boolean serviceOK() {
        int isAvailable = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);

        if (isAvailable == ConnectionResult.SUCCESS) {
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(isAvailable)) {
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(this, isAvailable, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, "Can't connect to mapping service", Toast.LENGTH_SHORT).show();
        }

        return false;
    }

    private void initMap() {
        if (mMap == null) {
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);

        }
    }

    private void goToLocation(double lat, double lng, float zoom) {
        LatLng locationLatLng = new LatLng(lat, lng);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(locationLatLng, zoom);
        mMap.moveCamera(update);
    }

    @Override
    public void onSuccess(Location location) {

        if (location != null) {
            goToLocation(location.getLatitude(), location.getLongitude(), 15);

            LatLngBounds screenBounds = mMap.getProjection().getVisibleRegion().latLngBounds;

            PolygonOptions po = new PolygonOptions();

            LatLng northwest = new LatLng(screenBounds.southwest.latitude, screenBounds.northeast.longitude);
            LatLng southeast = new LatLng(screenBounds.northeast.latitude, screenBounds.southwest.longitude);

            po.add(screenBounds.northeast, northwest);
            po.add(northwest, screenBounds.southwest);
            po.add(screenBounds.southwest, southeast);
            po.add(southeast, screenBounds.northeast);

            po.fillColor(0x333300FF)
                    .strokeWidth(3)
                    .strokeColor(Color.BLUE);

            mMap.addPolygon(po);
        }


    }
}
