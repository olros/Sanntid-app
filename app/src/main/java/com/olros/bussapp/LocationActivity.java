package com.olros.bussapp;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.snackbar.Snackbar;
import com.google.ar.core.Frame;
import com.google.ar.core.Plane;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableException;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.olros.bussapp.rendering.LocationNode;
import com.olros.bussapp.rendering.LocationNodeRender;
import com.olros.bussapp.utils.ARLocationPermissionHelper;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

@TargetApi(24)
public class LocationActivity extends AppCompatActivity {
    private boolean installRequested;
    private boolean hasFinishedLoading = false;

    private Snackbar loadingMessageSnackbar = null;
    private Snackbar openActivityMessageSnackbar = null;

    private ArSceneView arSceneView;

    // Renderables for this example
    private ViewRenderable Renderable1;
    private ViewRenderable Renderable2;
    private ViewRenderable Renderable3;
    private ViewRenderable Renderable4;
    private ViewRenderable Renderable5;

    // Our ARCore-Location scene
    private LocationScene locationScene;

    @Override
    @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
    // CompletableFuture requires api level 24
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sceneform);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_ar);
        toolbar.setTitle(R.string.arcore);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LocationActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        arSceneView = findViewById(R.id.ar_scene_view);

        ArrayList<Model> arraylist = HoldeplassArray.getList();
        arraylist.remove(0);
        arraylist.remove(0);

        CompletableFuture<ViewRenderable> render1 = ViewRenderable.builder()
                .setView(this, R.layout.example_layout)
                .build();
        CompletableFuture<ViewRenderable> render2 = ViewRenderable.builder()
                .setView(this, R.layout.example_layout)
                .build();
        CompletableFuture<ViewRenderable> render3 = ViewRenderable.builder()
                .setView(this, R.layout.example_layout)
                .build();
        CompletableFuture<ViewRenderable> render4 = ViewRenderable.builder()
                .setView(this, R.layout.example_layout)
                .build();
        CompletableFuture<ViewRenderable> render5 = ViewRenderable.builder()
                .setView(this, R.layout.example_layout)
                .build();


        //CompletableFuture.allOf(render1, render2, render3, render4, render5, render6)
        CompletableFuture.allOf(render1, render2, render3, render4, render5)
                .handle(
                        (notUsed, throwable) -> {
                            // When you build a Renderable, Sceneform loads its resources in the background while
                            // returning a CompletableFuture. Call handle(), thenAccept(), or check isDone()
                            // before calling get().

                            if (throwable != null) {
                                DemoUtils.displayError(this, "Unable to load renderables", throwable);
                                return null;
                            }

                            try {
                                Renderable1 = render1.get();
                                Renderable2 = render2.get();
                                Renderable3 = render3.get();
                                Renderable4 = render4.get();
                                Renderable5 = render5.get();
                                hasFinishedLoading = true;

                            } catch (InterruptedException | ExecutionException ex) {
                                DemoUtils.displayError(this, "Unable to load renderables", ex);
                            }

                            return null;
                        });

        // Set an update listener on the Scene that will hide the loading message once a Plane is
        // detected.
        arSceneView
                .getScene()
                .addOnUpdateListener(
                        frameTime -> {
                            if (!hasFinishedLoading) {
                                return;
                            }

                            if (locationScene == null) {
                                // If our locationScene object hasn't been setup yet, this is a good time to do it
                                // We know that here, the AR components have been initiated.
                                locationScene = new LocationScene(this, this, arSceneView);

                                LocationMarker layoutLocationMarker1 = new LocationMarker(arraylist.get(0).getLocation().longitude, arraylist.get(0).getLocation().latitude, getEV1(), arraylist.get(0).getName(), findDistance(0), arraylist.get(0).getHoldeplass());
                                LocationMarker layoutLocationMarker2 = new LocationMarker(arraylist.get(1).getLocation().longitude, arraylist.get(1).getLocation().latitude, getEV2(), arraylist.get(1).getName(), findDistance(1), arraylist.get(1).getHoldeplass());
                                LocationMarker layoutLocationMarker3 = new LocationMarker(arraylist.get(2).getLocation().longitude, arraylist.get(2).getLocation().latitude, getEV3(), arraylist.get(2).getName(), findDistance(2), arraylist.get(2).getHoldeplass());
                                LocationMarker layoutLocationMarker4 = new LocationMarker(arraylist.get(3).getLocation().longitude, arraylist.get(3).getLocation().latitude, getEV4(), arraylist.get(3).getName(), findDistance(3), arraylist.get(3).getHoldeplass());
                                LocationMarker layoutLocationMarker5 = new LocationMarker(arraylist.get(4).getLocation().longitude, arraylist.get(4).getLocation().latitude, getEV5(), arraylist.get(4).getName(), findDistance(4), arraylist.get(4).getHoldeplass());
                                /*LocationMarker layoutLocationMarker6 = new LocationMarker(arraylist.get(5).getLocation().longitude, arraylist.get(5).getLocation().latitude, getEV6(), arraylist.get(5).getName(), findDistance(5), arraylist.get(5).getHoldeplass());
                                */

                                layoutLocationMarker1.setRenderEvent(new LocationNodeRender() {
                                    @Override
                                    public void render(LocationNode node) {
                                        View eView = Renderable1.getView();
                                        TextView nameTextView = eView.findViewById(R.id.textView);
                                        TextView distanceTextView = eView.findViewById(R.id.textView2);
                                        distanceTextView.setText(locationScene.mLocationMarkers.get(0).getDistance());
                                        nameTextView.setText(locationScene.mLocationMarkers.get(0).getName());
                                        nameTextView.setSelected(true);
                                    }
                                });
                                layoutLocationMarker2.setRenderEvent(new LocationNodeRender() {
                                    @Override
                                    public void render(LocationNode node) {
                                        View eView = Renderable2.getView();
                                        TextView nameTextView = eView.findViewById(R.id.textView);
                                        TextView distanceTextView = eView.findViewById(R.id.textView2);
                                        distanceTextView.setText(locationScene.mLocationMarkers.get(1).getDistance());
                                        nameTextView.setText(locationScene.mLocationMarkers.get(1).getName());
                                        nameTextView.setSelected(true);
                                    }
                                });
                                layoutLocationMarker3.setRenderEvent(new LocationNodeRender() {
                                    @Override
                                    public void render(LocationNode node) {
                                        View eView = Renderable3.getView();
                                        TextView nameTextView = eView.findViewById(R.id.textView);
                                        TextView distanceTextView = eView.findViewById(R.id.textView2);
                                        distanceTextView.setText(locationScene.mLocationMarkers.get(2).getDistance());
                                        nameTextView.setText(locationScene.mLocationMarkers.get(2).getName());
                                        nameTextView.setSelected(true);
                                    }
                                });
                                layoutLocationMarker4.setRenderEvent(new LocationNodeRender() {
                                    @Override
                                    public void render(LocationNode node) {
                                        View eView = Renderable4.getView();
                                        TextView nameTextView = eView.findViewById(R.id.textView);
                                        TextView distanceTextView = eView.findViewById(R.id.textView2);
                                        distanceTextView.setText(locationScene.mLocationMarkers.get(3).getDistance());
                                        nameTextView.setText(locationScene.mLocationMarkers.get(3).getName());
                                        nameTextView.setSelected(true);
                                    }
                                });
                                layoutLocationMarker5.setRenderEvent(new LocationNodeRender() {
                                    @Override
                                    public void render(LocationNode node) {
                                        View eView = Renderable5.getView();
                                        TextView nameTextView = eView.findViewById(R.id.textView);
                                        TextView distanceTextView = eView.findViewById(R.id.textView2);
                                        distanceTextView.setText(locationScene.mLocationMarkers.get(4).getDistance());
                                        nameTextView.setText(locationScene.mLocationMarkers.get(4).getName());
                                        nameTextView.setSelected(true);
                                    }
                                });

                                locationScene.mLocationMarkers.add(layoutLocationMarker1);
                                locationScene.mLocationMarkers.add(layoutLocationMarker2);
                                locationScene.mLocationMarkers.add(layoutLocationMarker3);
                                locationScene.mLocationMarkers.add(layoutLocationMarker4);
                                locationScene.mLocationMarkers.add(layoutLocationMarker5);

                            }

                            Frame frame = arSceneView.getArFrame();
                            if (frame == null) {
                                return;
                            }

                            if (frame.getCamera().getTrackingState() != TrackingState.TRACKING) {
                                return;
                            }

                            if (locationScene != null) {
                                locationScene.processFrame(frame);
                            }

                            if (loadingMessageSnackbar != null) {
                                for (Plane plane : frame.getUpdatedTrackables(Plane.class)) {
                                    if (plane.getTrackingState() == TrackingState.TRACKING) {
                                        hideLoadingMessage();
                                    }
                                }
                            }
                        });


        // Lastly request CAMERA & fine location permission which is required by ARCore-Location.
        ARLocationPermissionHelper.requestPermission(this);
    }

    private String findDistance(int index) {
        ArrayList<Model> arraylist = HoldeplassArray.getList();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Criteria criteria = new Criteria();
            LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            String provider = locationManager.getBestProvider(criteria, false);
            Location location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                double lat = location.getLatitude();
                double lng = location.getLongitude();
                LatLng mLatestLocation = new LatLng(lat, lng);
                LatLng pointLocation = arraylist.get(index).getLocation();
                String distance = Utils.formatDistanceBetween(mLatestLocation, pointLocation);
                if (TextUtils.isEmpty(distance)) {
                    return "Ukjent avstand";
                } else {
                    return distance;
                }
            } else {
                return "Ukjent avstand";
            }
        }
        return "Noe gikk galt";
    }

    /**
     * Example node of a layout
     * @return
     */
    private Node getEV1() {
        Node base = new Node();
        base.setRenderable(Renderable1);
        View eView = Renderable1.getView();
        eView.setOnTouchListener((v, event) -> {
            showSnackbar(0);
            return false;
        });
        return base;
    }
    private Node getEV2() {
        Node base = new Node();
        base.setRenderable(Renderable2);
        View eView = Renderable2.getView();
        eView.setOnTouchListener((v, event) -> {
            showSnackbar(1);
            return false;
        });
        return base;
    }
    private Node getEV3() {
        Node base = new Node();
        base.setRenderable(Renderable3);
        View eView = Renderable3.getView();
        eView.setOnTouchListener((v, event) -> {
            showSnackbar(2);
            return false;
        });
        return base;
    }
    private Node getEV4() {
        Node base = new Node();
        base.setRenderable(Renderable4);
        View eView = Renderable4.getView();
        eView.setOnTouchListener((v, event) -> {
            showSnackbar(3);
            return false;
        });
        return base;
    }
    private Node getEV5() {
        Node base = new Node();
        base.setRenderable(Renderable5);
        View eView = Renderable5.getView();
        eView.setOnTouchListener((v, event) -> {
            showSnackbar(4);
            return false;
        });
        return base;
    }

    private void showSnackbar(int index) {
        openActivityMessageSnackbar =
                Snackbar.make(
                        LocationActivity.this.findViewById(android.R.id.content),
                        locationScene.mLocationMarkers.get(index).getName(),
                        Snackbar.LENGTH_LONG);
        openActivityMessageSnackbar.setAction(R.string.see_departures, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LocationActivity.this, BusActivity.class);
                intent.putExtra("navn", locationScene.mLocationMarkers.get(index).getName());
                intent.putExtra("holdeplass", locationScene.mLocationMarkers.get(index).getHoldeplass());
                LocationActivity.this.startActivity(intent);
            }
        });
        openActivityMessageSnackbar.getView().setBackgroundColor(0xbf323232);
        openActivityMessageSnackbar.show();
    }

    /**
     * Make sure we call locationScene.resume();
     */
    @Override
    protected void onResume() {
        super.onResume();

        if (locationScene != null) {
            locationScene.resume();
        }

        if (arSceneView.getSession() == null) {
            // If the session wasn't created yet, don't resume rendering.
            // This can happen if ARCore needs to be updated or permissions are not granted yet.
            try {
                Session session = DemoUtils.createArSession(this, installRequested);
                if (session == null) {
                    installRequested = ARLocationPermissionHelper.hasPermission(this);
                    return;
                } else {
                    arSceneView.setupSession(session);
                }
            } catch (UnavailableException e) {
                DemoUtils.handleSessionException(this, e);
            }
        }

        try {
            arSceneView.resume();
        } catch (CameraNotAvailableException ex) {
            DemoUtils.displayError(this, "Unable to get camera", ex);
            finish();
            return;
        }

        if (arSceneView.getSession() != null) {
            showLoadingMessage();
        }
    }

    /**
     * Make sure we call locationScene.pause();
     */
    @Override
    public void onPause() {
        super.onPause();

        if (locationScene != null) {
            locationScene.pause();
        }

        arSceneView.pause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        arSceneView.destroy();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] results) {
        if (!ARLocationPermissionHelper.hasPermission(this)) {
            if (!ARLocationPermissionHelper.shouldShowRequestPermissionRationale(this)) {
                // Permission denied with checking "Do not ask again".
                ARLocationPermissionHelper.launchPermissionSettings(this);
            } else {
                Toast.makeText(
                        this, R.string.camera_permission_needed, Toast.LENGTH_LONG)
                        .show();
            }
            finish();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            // Standard Android full-screen functionality.
            getWindow()
                    .getDecorView()
                    .setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    private void showLoadingMessage() {
        if (loadingMessageSnackbar != null && loadingMessageSnackbar.isShownOrQueued()) {
            return;
        }

        loadingMessageSnackbar =
                Snackbar.make(
                        LocationActivity.this.findViewById(android.R.id.content),
                        R.string.plane_finding,
                        Snackbar.LENGTH_INDEFINITE);
        loadingMessageSnackbar.getView().setBackgroundColor(0xbf323232);
        loadingMessageSnackbar.show();
    }

    private void hideLoadingMessage() {
        if (loadingMessageSnackbar == null) {
            return;
        }

        loadingMessageSnackbar.dismiss();
        loadingMessageSnackbar = null;
    }
}