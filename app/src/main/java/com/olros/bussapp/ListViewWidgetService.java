package com.olros.bussapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ListViewWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {

        int appWidgetId = Integer.valueOf(intent.getData().getSchemeSpecificPart())
                - NewAppWidget.randomNumber;

        return (new AppWidgetListView(this.getApplicationContext(), intent));
    }
}

class AppWidgetListView implements RemoteViewsService.RemoteViewsFactory {

    private ArrayList<WidgetDataModel> dataList = new ArrayList<WidgetDataModel>();
    private Context context;
    private int appWidgetId;

    public AppWidgetListView(Context applicationContext, Intent intent) {
        this.context=applicationContext;
        appWidgetId = Integer.valueOf(intent.getData().getSchemeSpecificPart())
                - NewAppWidget.randomNumber;

        loadDepartures();
        NewAppWidget.updateWidget(context, appWidgetId);
    }

    private String getTimeUntil(String depTime) {
        DateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.UK);
        DateFormat targetFormat = new SimpleDateFormat("HH:mm", Locale.UK);
        Date endDate = null;
        Date currentTime = Calendar.getInstance().getTime();
        String until = "";
        try {
            endDate = originalFormat.parse(depTime);
            long diffInMs = endDate.getTime() - currentTime.getTime();

            int minutesUntil = (int) (diffInMs / 60000);

            switch (minutesUntil) {
                case 0:
                    until = "Nå";
                    break;
                case 1:
                    until = "1 min";
                    break;
                case 2:
                    until = "2 min";
                    break;
                case 3:
                    until = "3 min";
                    break;
                case 4:
                    until = "4 min";
                    break;
                case 5:
                    until = "5 min";
                    break;
                case 6:
                    until = "6 min";
                    break;
                case 7:
                    until = "7 min";
                    break;
                case 8:
                    until = "8 min";
                    break;
                case 9:
                    until = "9 min";
                    break;
                default:
                    until = targetFormat.format(endDate);
                    break;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return until;
    }

    private void loadDepartures() {

        String url = "https://sanntid.ga/api-entur-avganger.php?id=" + newConfigActivity.loadTitlePref(context, appWidgetId) + "&antall=15";

        StringRequest jsonArrayRequest = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    dataList.clear();
                    JSONObject jsonObject = new JSONObject(response);
                    JSONObject data = jsonObject.getJSONObject("data");
                    JSONObject stopPlace = data.getJSONObject("stopPlace");
                    JSONArray jsonArray = stopPlace.getJSONArray("estimatedCalls");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject oneObject = jsonArray.getJSONObject(i);
                        Boolean realtime = oneObject.getBoolean("realtime");
                        //String time = oneObject.getString("expectedDepartureTime").substring(11,16);
                        String depTime = oneObject.getString("expectedDepartureTime");
                        String time = getTimeUntil(depTime);
                        JSONObject destinationDisplay = oneObject.getJSONObject("destinationDisplay");
                        String frontText = destinationDisplay.getString("frontText");
                        JSONObject serviceJourney = oneObject.getJSONObject("serviceJourney");
                        JSONObject journeyPattern = serviceJourney.getJSONObject("journeyPattern");
                        JSONObject line = journeyPattern.getJSONObject("line");
                        String publicCode = line.getString("publicCode");
                        String lineId = line.getString("id");

                        WidgetDataModel model = new WidgetDataModel();
                        model.realtime = realtime;
                        model.time = time;
                        model.frontText = frontText;
                        model.publicCode = publicCode;
                        model.lineId = lineId;

                        dataList.add(model);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    dataList.clear();
                    WidgetDataModel model = new WidgetDataModel();
                    model.realtime = false;
                    model.time = "--:--";
                    model.frontText = "Noe gikk galt";
                    model.publicCode = "--";
                    model.lineId = "0";

                    dataList.add(model);
                }
                Toast.makeText(context, "Lastet avganger", Toast.LENGTH_SHORT).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Volley", error.toString());
                dataList.clear();
                WidgetDataModel model = new WidgetDataModel();
                model.realtime = false;
                model.time = "--:--";
                model.frontText = "Noe gikk galt, prøv på nytt";
                model.publicCode = "-";
                model.lineId = "0";

                dataList.add(model);
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(jsonArrayRequest);
    }

    @Override
    public void onCreate() {
        loadDepartures();
    }

    @Override
    public void onDataSetChanged() {
        loadDepartures();
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_list_item);

        views.setTextViewText(R.id.destination, dataList.get(position).frontText);
        views.setTextViewText(R.id.lineSign, dataList.get(position).publicCode);

        /* Forsøk på så sette dynamisk bakgrunnsfarge med border radius
        int color = Color.rgb(255,0,0); //red for example
        int radius = 5; //radius will be 5px
        int strokeWidth = 2;
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(color);
        gradientDrawable.setCornerRadius(radius);
        gradientDrawable.setStroke(strokeWidth, color);
        button.setBackground(gradientDrawable);
        views.setInt(R.id.lineSign, "setBackground", gradientDrawable);*/

        if (dataList.get(position).realtime) {
            views.setTextViewText(R.id.time, dataList.get(position).time);
            views.setTextColor(R.id.time, context.getResources().getColor(R.color.colorThird));
        } else {
            views.setTextViewText(R.id.time, "ca. " + dataList.get(position).time);
            views.setTextColor(R.id.time, context.getResources().getColor(R.color.colorBlack));
        }

        Intent fillInIntent = new Intent();
        fillInIntent.putExtra("frontText",dataList.get(position).frontText);
        views.setOnClickFillInIntent(R.id.parentView, fillInIntent);
        return views;
    }

    @Override
    public RemoteViews getLoadingView() {

        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}