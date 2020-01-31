package com.olros.bussapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.util.ArrayList;

public class WidgetDataModel {

    public static final String PREF_KEY_JSON = "jsonData";
    public Boolean realtime;
    public String time, frontText, publicCode, lineId;

    /*public static void createSampleDataForWidget(Context context) {
        Toast.makeText(context, "createSampleDataForWidget", Toast.LENGTH_SHORT).show();
        SharedPreferences sharedPref = context.getSharedPreferences("SharedPrefs", Context.MODE_PRIVATE);
        String json = sharedPref.getString(PREF_KEY_JSON, "[]");

        JSONArray jsonArray;
        try {
            jsonArray=new JSONArray(json);

            if(jsonArray.length()==0) {
                for (int i = 0; i < 10; i++) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("title", "Tittel: " + (i + 1));
                    jsonObject.put("subTitle", (i + 1) + " min");

                    jsonArray.put(jsonObject);
                }
                sharedPref.edit().putString(PREF_KEY_JSON,jsonArray.toString()).apply();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }*/

}