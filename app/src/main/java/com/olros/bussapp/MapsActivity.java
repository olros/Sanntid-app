package com.olros.bussapp;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

public class MapsActivity extends AppCompatActivity implements GoogleMap.OnInfoWindowClickListener, GoogleMap.OnCameraIdleListener, OnMapReadyCallback {

    private GoogleMap mMap;
    private LatLng mLatestLocation;
    private ArrayList<String> hId = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_maps);
        toolbar.setTitle(R.string.map);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapsActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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

        mMap.moveCamera(CameraUpdateFactory.zoomTo(15.0f));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Criteria criteria = new Criteria();
            LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            String provider = locationManager.getBestProvider(criteria, false);
            Location location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                double lat = location.getLatitude();
                double lng = location.getLongitude();
                LatLng mLatestLocation = new LatLng(lat, lng);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(mLatestLocation));
            } else {
                LatLng mLatestLocation = new LatLng(58.1524739,7.9936183);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(mLatestLocation));
            }
        } else {
            LatLng mLatestLocation = new LatLng(58.1524739,7.9936183);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(mLatestLocation));
        }

        String s = getString(R.string.click_to_see_departures);

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.loading_stopplaces));
        progressDialog.show();

        String url = "";

        if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Criteria criteria = new Criteria();
            LocationManager locationManager = (LocationManager) MapsActivity.this.getSystemService(Context.LOCATION_SERVICE);
            String provider = locationManager.getBestProvider(criteria, false);
            Location location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                mLatestLocation = new LatLng(location.getLatitude(), location.getLongitude());
                url = "https://api.entur.io/geocoder/v1/reverse?point.lat=" + location.getLatitude() + "&point.lon=" + location.getLongitude() + "&lang=en&size=30&layers=venue";
                mMap.setMyLocationEnabled(true);
            } else {
                url = "https://api.entur.io/geocoder/v1/reverse?point.lat=58.158507&point.lon=8.012126&lang=en&size=30&layers=venue";
            }
        } else {
            url = "https://api.entur.io/geocoder/v1/reverse?point.lat=58.158507&point.lon=8.012126&lang=en&size=30&layers=venue";
        }

        StringRequest jsonArrayRequest = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONArray("features");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject oneObject = jsonArray.getJSONObject(i);
                        JSONObject properties = oneObject.getJSONObject("properties");
                        String navn = properties.getString("name");
                        String holdeplassId = properties.getString("id").substring(14);
                        JSONObject geometry = oneObject.getJSONObject("geometry");
                        JSONArray coordinates = geometry.getJSONArray("coordinates");
                        ArrayList<Horizontal> emptyList = new ArrayList<>();
                        double latitude = coordinates.getDouble(1);
                        double longitude = coordinates.getDouble(0);
                        float id = Float.parseFloat(holdeplassId);
                        hId.add(holdeplassId);
                        mMap.addMarker(new MarkerOptions().position(new LatLng(latitude,longitude)).zIndex(id).title(navn).snippet(s));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    progressDialog.dismiss();
                }
                progressDialog.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Volley", error.toString());
                progressDialog.dismiss();
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonArrayRequest);

        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnCameraIdleListener((GoogleMap.OnCameraIdleListener) this);
    }

    @Override
    public void onCameraIdle() {
        LatLng mapCenter = mMap.getCameraPosition().target;
        String s = getString(R.string.click_to_see_departures);
        String url = "https://api.entur.io/geocoder/v1/reverse?point.lat=" + mapCenter.latitude + "&point.lon=" + mapCenter.longitude + "&lang=en&size=30&layers=venue";

        StringRequest jsonArrayRequest = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONArray("features");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject oneObject = jsonArray.getJSONObject(i);
                        JSONObject properties = oneObject.getJSONObject("properties");
                        String navn = properties.getString("name");
                        String holdeplassId = properties.getString("id").substring(14);
                        JSONObject geometry = oneObject.getJSONObject("geometry");
                        JSONArray coordinates = geometry.getJSONArray("coordinates");
                        double latitude = coordinates.getDouble(1);
                        double longitude = coordinates.getDouble(0);
                        float id = Float.parseFloat(holdeplassId);
                        if (hId.contains(holdeplassId)) {
                            // Marker already exists, don't add
                        } else {
                            // Marker doesn't exist
                            hId.add(holdeplassId);
                            mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).zIndex(id).title(navn).snippet(s));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Volley", error.toString());
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonArrayRequest);
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        String navn = marker.getTitle();
        String holdeplass = Float.toString(marker.getZIndex());
        String result = holdeplass.substring(0, holdeplass.length() - 2);
        // Toast.makeText(MapsActivity.this, navn + ", " + result, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(MapsActivity.this, BusActivity.class);
        intent.putExtra("navn", navn);
        intent.putExtra("holdeplass", result);
        MapsActivity.this.startActivity(intent);
    }
}
