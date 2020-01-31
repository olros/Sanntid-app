package com.olros.bussapp;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.snackbar.Snackbar;
import com.google.maps.android.SphericalUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class MainActivity extends AppCompatActivity {

    Toolbar toolbar, searchtollbar;
    Menu search_menu;
    private RecyclerView recyclerView;
    private RecyclerView horizontalRecyclerView;
    MenuItem item_search;
    ArrayList<Model> holdeplasser = new ArrayList<>();
    ArrayList<Horizontal> horizontalList = new ArrayList<>();
    ModelAdapter mAdapter;
    HorizontalAdapter horizontalAdapter;
    private static final int PERMISSION_REQ = 0;
    private LatLng mLatestLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check fine location permission has been granted
        if (!Utils.checkFineLocationPermission(this)) {
            // See if user has denied permission in the past
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show a simple snackbar explaining the request instead
                showPermissionSnackbar();
            } else {
                // Otherwise request permission from user
                if (savedInstanceState == null) {
                    requestFineLocationPermission();
                }
            }
        } else {
            // Otherwise permission is granted (which is always the case on pre-M devices)
            fineLocationPermissionGranted();
        }

        initControls();

        setShortcuts();
    }

    /**
     * Permissions request result callback
     */
    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQ:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // Permission was granted
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                    }
                    Criteria criteria = new Criteria();
                    LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
                    String provider = locationManager.getBestProvider(criteria, false);
                    Location location = locationManager.getLastKnownLocation(provider);
                    if (location != null) {
                        loadData();
                    }

                } else {

                    new AlertDialog.Builder(this)
                            .setMessage(R.string.text_location_denied)
                            .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //
                                }
                            })
                            .create()
                            .show();

                }
                return;
        }
    }

    /**
     * Request the fine location permission from the user
     */
    private void requestFineLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQ);
    }

    /**
     * Run when fine location permission has been granted
     */
    private void fineLocationPermissionGranted() {
        UtilityService.requestLocation(this);
        Log.v("Posisjon", "Tillatt");
    }

    /**
     * Show a location-permission asking snackbar
     */
    private void showPermissionSnackbar() {
        Snackbar.make(
                findViewById(R.id.snackbar_container), R.string.snackbar_permission_explanation, Snackbar.LENGTH_LONG)
                .setAction(R.string.permission_explanation_action, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showPermissionDialog();
                        //requestFineLocationPermission();
                    }
                })
                .show();
    }

    /**
     * Show a permission explanation dialog
     */
    private void showPermissionDialog() {
        new AlertDialog.Builder(this)
            .setTitle(R.string.dialog_permission_title)
            .setMessage(R.string.dialog_permission_explanation)
            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    requestFineLocationPermission();
                }
            })
            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                }
            })
            .create()
            .show();
    }

    /**
     * Show a basic debug dialog to provide more info on the built-in debug
     * options.
     */
    private void showDebugDialog(int titleResId, int bodyResId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(titleResId)
                .setMessage(bodyResId)
                .setPositiveButton(android.R.string.ok, null);
        builder.create().show();
    }

    private void initControls() {

        /*toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setSearchToolbar();*/

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        loadData();

        recyclerView.setAdapter(mAdapter);
        HoldeplassArray.setList(holdeplasser);

        // Sorter holdeplasser basert på avstand fra bruker hvis posisjonstillatelse er gitt
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Criteria criteria = new Criteria();
            LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            String provider = locationManager.getBestProvider(criteria, false);
            Location location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                double lat = location.getLatitude();
                double lng = location.getLongitude();
                LatLng mLatestLocation = new LatLng(lat, lng);
                //ArrayList<Model> holdeplasser = loadAttractionsFromLocation(mLatestLocation);
                mAdapter = new ModelAdapter(this, loadAttractionsFromLocation(mLatestLocation));
            }
        } else {
            // Posisjonstillatelse er ikke gitt, altså kke sorter
            mAdapter = new ModelAdapter(this, holdeplasser);
        }

        //mAdapter = new ModelAdapter(this, holdeplasser);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mAdapter);

        final SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout) this.findViewById(R.id.swipeToRefresh);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                /*if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    Criteria criteria = new Criteria();
                    LocationManager locationManager = (LocationManager) MainActivity.this.getSystemService(Context.LOCATION_SERVICE);
                    String provider = locationManager.getBestProvider(criteria, false);
                    Location location = locationManager.getLastKnownLocation(provider);
                    if (location != null) {
                        mLatestLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        mAdapter.holdeplasser = loadAttractionsFromLocation(mLatestLocation);
                        mAdapter.notifyDataSetChanged();
                        Toast.makeText(MainActivity.this, R.string.refresh_finished, Toast.LENGTH_SHORT).show();
                        swipeLayout.setRefreshing(false);
                    } else {
                        Toast.makeText(MainActivity.this, R.string.something_went_wrong, Toast.LENGTH_SHORT).show();
                        swipeLayout.setRefreshing(false);
                    }
                } else {
                    Toast.makeText(MainActivity.this, R.string.give_location_to_sort, Toast.LENGTH_SHORT).show();
                    swipeLayout.setRefreshing(false);
                }*/
                loadData();
                Toast.makeText(MainActivity.this, R.string.refresh_finished, Toast.LENGTH_SHORT).show();
                swipeLayout.setRefreshing(false);
            }
        });
    }

    /*
    *
    * Fiks så de to første elementene forblir, mens resten slettes og erstattes med ny og oppdaterte data i loadData()!!!
    *
     */

    void loadData() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.loading_stopplaces));
        progressDialog.show();

        String url = "https://api.entur.io/geocoder/v1/reverse?point.lat=58.158507&point.lon=8.012126&lang=en&size=30&layers=venue";

        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Criteria criteria = new Criteria();
            LocationManager locationManager = (LocationManager) MainActivity.this.getSystemService(Context.LOCATION_SERVICE);
            String provider = locationManager.getBestProvider(criteria, false);
            Location location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                mLatestLocation = new LatLng(location.getLatitude(), location.getLongitude());
                url = "https://api.entur.io/geocoder/v1/reverse?point.lat=" + location.getLatitude() + "&point.lon=" + location.getLongitude() + "&lang=en&size=30&layers=venue";
            }
        }

        StringRequest jsonArrayRequest = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (holdeplasser.size() > 1) {
                    holdeplasser.subList(1, holdeplasser.size()).clear();
                } else {
                    ArrayList<Horizontal> emptyTopList = new ArrayList<>();
                    holdeplasser.add(new Model(new LatLng(0.0, 0.0), "0", "Top row", Model.ItemType.top_item, emptyTopList));
                }
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
                        holdeplasser.add(new Model(new LatLng(latitude,longitude), holdeplassId, navn, Model.ItemType.list_item, emptyList));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    progressDialog.dismiss();
                }
                mAdapter.holdeplasser = loadAttractionsFromLocation(mLatestLocation);
                mAdapter.notifyDataSetChanged();
                recyclerView.setAdapter(mAdapter);
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
    }

    public void loadFavourites() {
        if (holdeplasser.size() > 1) {
            holdeplasser.subList(1, holdeplasser.size()).clear();
        } else {
            ArrayList<Horizontal> emptyTopList = new ArrayList<>();
            holdeplasser.add(new Model(new LatLng(0.0, 0.0), "0", "Top row", Model.ItemType.top_item, emptyTopList));

            ArrayList<Horizontal> horizontalEmptyList = new ArrayList<>();
            SharedPreferences pref = getApplicationContext().getSharedPreferences("Favourites", 0);
            Map<String, ?> allEntries = pref.getAll();
            for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                horizontalEmptyList.add(new Horizontal(entry.getKey(), entry.getValue().toString(), R.drawable.ic_star_border, "com.olros.bussapp.BusActivity"));
            }

            //holdeplasser.add(new Model(new LatLng(0.0, 0.0), "0", "Horizontal", Model.ItemType.horizontal_item, horizontalEmptyList));
        }

        SharedPreferences pref = getApplicationContext().getSharedPreferences("Favourites", 0);
        Map<String, ?> allEntries = pref.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            ArrayList<Horizontal> emptyList = new ArrayList<>();
            holdeplasser.add(new Model(new LatLng(0.0,0.0), entry.getKey(), entry.getValue().toString(), Model.ItemType.card_item, emptyList));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mBroadcastReceiver, UtilityService.getLocationUpdatedIntentFilter());
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location =
                    intent.getParcelableExtra(FusedLocationProviderApi.KEY_LOCATION_CHANGED);
            if (location != null) {
                mLatestLocation = new LatLng(location.getLatitude(), location.getLongitude());
                mAdapter.holdeplasser = loadAttractionsFromLocation(mLatestLocation);
                mAdapter.notifyDataSetChanged();
            }
        }
    };

    public ArrayList<Model> loadAttractionsFromLocation(final LatLng curLatLng) {
        if (curLatLng != null) {
            Collections.sort(holdeplasser,
                    new Comparator<Model>() {
                        @Override
                        public int compare(Model lhs, Model rhs) {
                            if (rhs.getType() == Model.ItemType.horizontal_item || lhs.getType() == Model.ItemType.horizontal_item || rhs.getType() == Model.ItemType.top_item || lhs.getType() == Model.ItemType.top_item) {
                                double lhsDistance = SphericalUtil.computeDistanceBetween(
                                        curLatLng, curLatLng);
                                double rhsDistance = SphericalUtil.computeDistanceBetween(
                                        curLatLng, curLatLng);
                                return (int) (lhsDistance - rhsDistance);
                            }
                            double lhsDistance = SphericalUtil.computeDistanceBetween(
                                    lhs.location, curLatLng);
                            double rhsDistance = SphericalUtil.computeDistanceBetween(
                                    rhs.location, curLatLng);
                            return (int) (lhsDistance - rhsDistance);
                        }
                    }
            );
        }
        return holdeplasser;
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean setShortcuts() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);
            shortcutManager.removeAllDynamicShortcuts();

            SharedPreferences sharedpref = getApplicationContext().getSharedPreferences("Favourites", 0);
            Map<String, ?> allEntries = sharedpref.getAll();
            int i = 0;
            shortcuts:
            for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                ShortcutInfo nr1 = new ShortcutInfo.Builder(this, entry.getKey())
                        .setShortLabel(entry.getValue().toString())
                        .setLongLabel(entry.getValue().toString())
                        .setIcon(Icon.createWithResource(this, R.drawable.ic_menu_bus_green))
                        .setIntents(
                                new Intent[]{
                                        new Intent(Intent.ACTION_MAIN, Uri.EMPTY, this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK),
                                        new Intent(this, BusActivity.class)
                                                .setAction(Intent.ACTION_VIEW)
                                                .putExtra("navn", entry.getValue().toString())
                                                .putExtra("holdeplass", entry.getKey())
                                })
                        .build();
                shortcutManager.addDynamicShortcuts(Arrays.asList(nr1));
                i++;
                if (i == 5) {
                    break shortcuts;
                }
            }
        }
        return true;
    }
}
