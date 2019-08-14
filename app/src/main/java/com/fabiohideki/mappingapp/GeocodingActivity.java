package com.fabiohideki.mappingapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GeocodingActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int ERROR_DIALOG_REQUEST = 9001;
    private static String TAG = "Fabio";
    private static final int PERM_REQUEST_CODE = 200;
    private static final String[] perms = {"android.permission.FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION"};

    private Marker marker;
    List<Marker> markers = new ArrayList<>();

    GoogleMap mMap;
    private static final double
            SEATTLE_LAT = 47.60621,
            SEATTLE_LNG = -122.33207,
            SYDNEY_LAT = -33.867487,
            SYDNEY_LNG = 151.20699,
            NEWYORK_LAT = 40.714353,
            NEWYORK_LNG = -74.005973;

    private Marker marker1, marker2;
    private Polyline line;

    private static final int POLYGON_POINTS = 4;

    private Polygon shape;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geocoding);
    }

    @Override
    protected void onResume() {
        super.onResume();

        initMap();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        Toast.makeText(this, "Ready to map!", Toast.LENGTH_SHORT).show();
        gotoLocation(SYDNEY_LAT, SYDNEY_LNG, 15);

        if (needPermissions()) return;

        init();

    }

    private void init() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {

                View view = getLayoutInflater().inflate(R.layout.info_window, null);
                TextView tvLocality = view.findViewById(R.id.tvLocality);
                TextView tvLat = view.findViewById(R.id.tvLat);
                TextView tvLng = view.findViewById(R.id.tvLng);
                TextView tvSnippet = view.findViewById(R.id.tvSnippet);

                LatLng latLng = marker.getPosition();
                tvLocality.setText(marker.getTitle());
                tvLat.setText("Lat: " + latLng.latitude);
                tvLng.setText("Lng: " + latLng.longitude);
                tvSnippet.setText(marker.getSnippet());

                return view;
            }
        });

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                Geocoder gc = new Geocoder(GeocodingActivity.this);
                List<Address> list = null;

                try {

                    list = gc.getFromLocation(latLng.latitude, latLng.longitude, 1);

                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }

                Address add = list.get(0);
                GeocodingActivity.this.addMarker(add, latLng.latitude, latLng.longitude);
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                String message = marker.getTitle() + " lat: " + marker.getPosition().longitude + " lng: " + marker.getPosition().longitude;
                Toast.makeText(GeocodingActivity.this, message, Toast.LENGTH_SHORT).show();
                //marker.remove();
                return false;
            }
        });

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {

                Geocoder gc = new Geocoder(GeocodingActivity.this);
                List<Address> list = null;

                try {

                    list = gc.getFromLocation(marker.getPosition().latitude, marker.getPosition().longitude, 1);

                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }

                Address add = list.get(0);
                marker.setTitle(add.getLocality());
                marker.setSnippet(add.getCountryName());
                marker.showInfoWindow();

            }
        });

    }

    private void addMarker(Address add, double lat, double lng) {

        if (markers.size() == POLYGON_POINTS) {
            removeEverything();
        }

        MarkerOptions options = new MarkerOptions()
                .title(add.getLocality())
                .position(new LatLng(lat, lng))
                .draggable(true)
                .icon(BitmapDescriptorFactory.defaultMarker());
//              .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_marker));
        String country = add.getCountryName();
        if (country.length() > 0) {
            options.snippet(country);
        }

        markers.add(mMap.addMarker(options));

        if (markers.size() == POLYGON_POINTS) {
            drawPolygon();
        }

    }

    private void drawLine() {
        PolylineOptions polylineOptions = new PolylineOptions()
                .add(marker1.getPosition())
                .add(marker2.getPosition());

        line = mMap.addPolyline(polylineOptions);
    }

    private void drawPolygon() {
        PolygonOptions options = new PolygonOptions()
                .fillColor(0x330000FF)
                .strokeWidth(3)
                .strokeColor(Color.BLUE);

        for (int i = 0; i < POLYGON_POINTS; i++) {
            options.add(markers.get(i).getPosition());
        }

        shape = mMap.addPolygon(options);


    }

    private void removeEverything() {

        for (Marker marker : markers) {
            marker.remove();
        }

        markers.clear();

        if (shape != null) {
            shape.remove();
            shape = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private boolean needPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    Activity#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                Toast.makeText(this, "Need Location permission", Toast.LENGTH_SHORT).show();
                requestPermissions(perms, PERM_REQUEST_CODE);
                return true;
            }
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {

            case PERM_REQUEST_CODE:

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    init();
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(perms, PERM_REQUEST_CODE);
                    }
                }
                break;
        }
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_geo);
        mapFragment.getMapAsync(this);

    }

    private void gotoLocation(double lat, double lng, float zoom) {
        LatLng latLng = new LatLng(lat, lng);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
        mMap.moveCamera(update);
    }

    private void hideSoftKeyboard(View v) {
        InputMethodManager imm =
                (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    public void geoLocate(View v) throws IOException {

        hideSoftKeyboard(v);

        TextView tv = (TextView) findViewById(R.id.editText1);

        if (tv == null || tv.getText().toString().equals(""))
            return;

        String searchString = tv.getText().toString();
        Toast.makeText(this, "Searching for: " + searchString, Toast.LENGTH_SHORT).show();

        Geocoder gc = new Geocoder(this);
        List<Address> list = gc.getFromLocationName(searchString, 1);

        if (list != null && list.size() > 0) {
            Address add = list.get(0);
            String locality = add.getLocality();
            Toast.makeText(this, "Found: " + locality, Toast.LENGTH_SHORT).show();

            double lat = add.getLatitude();
            double lng = add.getLongitude();
            gotoLocation(lat, lng, 15);

            if (marker != null) {
                marker.remove();
            }

            MarkerOptions options = new MarkerOptions()
                    .title(locality)
                    .position(new LatLng(lat, lng))
                    //.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_marker));

            String country = add.getCountryName();

            if (country.length() > 0) {
                options.snippet(country);
            }

            marker = mMap.addMarker(options);
        }


    }


    /* Menus Configurations */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.mapTypeNormal:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case R.id.mapTypeSatellite:
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.mapTypeTerrain:
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            case R.id.mapTypeHybrid:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
            case R.id.mapTypeNone:
                mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                break;
        }
        //noinspection SimplifiableIfStatement
/*        if (id == R.id.action_settings) {
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }

}
