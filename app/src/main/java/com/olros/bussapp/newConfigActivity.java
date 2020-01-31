package com.olros.bussapp;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Spinner;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Map;

import static com.olros.bussapp.NewAppWidget.UPDATE_LIST;

public class newConfigActivity extends Activity {

    private ArrayList<WidgetFavoritter> favList = new ArrayList<>();
    private static final String PREFS_NAME = "AppWidget";
    private static final String PREF_PREFIX_KEY = "appwidget";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.configuration_activity);
        setResult(RESULT_CANCELED);

        final Spinner spinner = (Spinner)findViewById(R.id.spinner);
        View btnCreate = findViewById(R.id.btn_go);

        SharedPreferences pref = getApplicationContext().getSharedPreferences("Favourites", 0);
        Map<String, ?> allEntries = pref.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            favList.add(new WidgetFavoritter(entry.getKey(), entry.getValue().toString()));

        }

        ArrayList<String> spnOptions = new ArrayList<>();
        for (int j = 0; j < pref.getAll().size(); j++) {
            spnOptions.add(favList.get(j).navn);
        }

        //set adapter for the spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, spnOptions);
        spinner.setAdapter(adapter);

        Button setupWidget = (Button) findViewById(R.id.setupWidget);
        setupWidget.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String selected_id = favList.get(spinner.getSelectedItemPosition()).id;
                String selected_name = favList.get(spinner.getSelectedItemPosition()).navn;
                handleSetupWidget(selected_id, selected_name);
            }
        });

    }

    private void handleSetupWidget(String selected_id, String selected_name) {
        showAppWidget(selected_id, selected_name);
    }

    int appWidgetId;

    private void showAppWidget(String selected_id, String selected_name) {

        appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            appWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);

            if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
                finish();
            }

            saveTitlePref(getApplicationContext(), appWidgetId, selected_id, selected_name);

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
            NewAppWidget.updateAppWidget(getApplicationContext(), appWidgetManager, appWidgetId);
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widgetList);

            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        }

    }

    public static class SharedObject {
        public String id, name;
    }

    // Write the prefix to the SharedPreferences object for this widget
    static void saveTitlePref(Context context, int appWidgetId, String id, String name) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        SharedObject newObj = new SharedObject();
        newObj.id = id;
        newObj.name = name;
        Gson gson = new Gson();
        String json = gson.toJson(newObj);
        prefs.putString(PREF_PREFIX_KEY + appWidgetId, json);
        prefs.apply();
    }

    // Read the prefix from the SharedPreferences object for this widget.
    // If there is no preference saved, get the default from a resource
    static String loadTitlePref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        Gson gson = new Gson();
        if (prefs.contains(PREF_PREFIX_KEY + appWidgetId)) {
            String titleValue = prefs.getString(PREF_PREFIX_KEY + appWidgetId, null);
            SharedObject obj = gson.fromJson(titleValue, SharedObject.class);
            return obj.id;
        } else {
            return "22002";
        }
    }

    static String loadNamePref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        Gson gson = new Gson();
        if (prefs.contains(PREF_PREFIX_KEY + appWidgetId)) {
            String titleValue = prefs.getString(PREF_PREFIX_KEY + appWidgetId, null);
            SharedObject nameObj = gson.fromJson(titleValue, SharedObject.class);
            return nameObj.name;
        } else {
            return "Vent litt";
        }
    }

    static void deleteTitlePref(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_PREFIX_KEY + appWidgetId);
        prefs.apply();
    }
}
