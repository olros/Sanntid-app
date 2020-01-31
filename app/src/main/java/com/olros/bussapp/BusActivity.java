package com.olros.bussapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.Map;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class BusActivity extends AppCompatActivity {

    private WebView mWebView;
    private String link;
    public boolean is_fav = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus);

        final String intent_navn = getIntent().getStringExtra("navn");
        final String intent_holdeplass = getIntent().getStringExtra("holdeplass");


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_bus);
        toolbar.setTitle(intent_navn);

        BottomAppBar bottomAppBar = (BottomAppBar) findViewById(R.id.bottom_app_bar);
        bottomAppBar.replaceMenu(R.menu.menu_bus_bar);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent intent = new Intent(BusActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);*/
                finish();
            }
        });

        bottomAppBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomNavigationDrawerFragment bottomNavDrawerFragment = new BottomNavigationDrawerFragment();
                bottomNavDrawerFragment.show(getSupportFragmentManager(), bottomNavDrawerFragment.getTag());
            }
        });

        SharedPreferences pref = getSharedPreferences("Favourites", 0);
        if(pref.contains(intent_holdeplass)){
            is_fav = true;
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (is_fav == true) {
            fab.setImageResource(R.drawable.ic_star_filled_white);
        } else {
            fab.setImageResource(R.drawable.ic_star_border);
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                if (is_fav == false) {
                    fab.setImageResource(R.drawable.ic_star_filled_white);
                    is_fav = true;

                    SharedPreferences pref = getApplicationContext().getSharedPreferences("Favourites", 0);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString(intent_holdeplass, intent_navn);
                    editor.apply();

                    Snackbar.make(view, intent_navn + " " + getString(R.string.was_added), Snackbar.LENGTH_LONG)
                            .setAction(R.string.see_favourites, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(BusActivity.this, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                }
                            }).show();

                } else if (is_fav == true) {
                    fab.setImageResource(R.drawable.ic_star_border);
                    is_fav = false;

                    SharedPreferences pref = getApplicationContext().getSharedPreferences("Favourites", 0);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.remove(intent_holdeplass);
                    editor.apply();

                    Snackbar.make(view, intent_navn + " " + getString(R.string.was_removed), Snackbar.LENGTH_LONG)
                            .setAction(R.string.home, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(BusActivity.this, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                }
                            }).show();
                }
            }
        });

        mWebView = (WebView) findViewById(R.id.activity_main_webview);

        mWebView.setFocusable(true);
        mWebView.setFocusableInTouchMode(true);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setDatabaseEnabled(true);
        mWebView.setWebViewClient(new WebViewClient());

        mWebView.setWebChromeClient(new WebChromeClient() {
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }
        });

        //link = "http://sanntid.ga/holdeplass.html?h=" + intent_holdeplass + "&app=true";
        link = "file:android_asset/holdeplass.html?h=" + intent_holdeplass;
        mWebView.loadUrl(link);
        final SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout) this.findViewById(R.id.swipeToRefresh);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mWebView.reload(); // refreshes the WebView
                mWebView.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        swipeLayout.setRefreshing(false);
                    }
                });
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //noinspection SimplifiableIfStatement
        //if (id == R.id.action_settings) {
        switch (item.getItemId()) {
            default:
                return true;
        }
    }
}