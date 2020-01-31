package com.olros.bussapp;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import java.text.DateFormat;
import java.util.Date;

import android.widget.Toast;

public class NewAppWidget extends AppWidgetProvider {

    static public int randomNumber = 72;
    public static String UPDATE_LIST = "UPDATE_LIST";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        Toast.makeText(context, "updateAppWidget", Toast.LENGTH_SHORT).show();
        String timeString =
                DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date());

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.new_app_widget);
        views.setTextViewText(R.id.update_value, "Sist oppdatert ??: " + timeString);
        views.setTextViewText(R.id.id_label, newConfigActivity.loadNamePref(context, appWidgetId));

        Intent intentUpdate = new Intent(context, NewAppWidget.class);
        intentUpdate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intentUpdate.setAction(UPDATE_LIST);

        PendingIntent pendingUpdate = PendingIntent.getBroadcast( context, appWidgetId, intentUpdate, 0);
        views.setOnClickPendingIntent(R.id.update, pendingUpdate);

        Intent intent = new Intent(context, ListViewWidgetService.class);
        intent.setData(Uri.fromParts("content", String.valueOf(appWidgetId + randomNumber), null));
        views.setRemoteAdapter(R.id.widgetList, intent);

        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widgetList);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
            updateWidget(context, appWidgetId);
            Toast.makeText(context, "Oppdateringen var vellykket! - onUpdate", Toast.LENGTH_SHORT).show();
        }

        //appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds,  R.id.widgetList);
        //super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if(intent.getAction().equalsIgnoreCase(UPDATE_LIST)){
            Toast.makeText(context, "onReceive", Toast.LENGTH_SHORT).show();
            Log.i("flow", "onReceive");
            int widgetId = intent.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            updateWidget(context, widgetId);
        } else {
            Log.i("flow", "onReceive - else");
        }
    }

    static void updateWidget(Context context, int widgetId) {
        Log.i("flow", "updateWidget");
        Toast.makeText(context, "updateWidget - function", Toast.LENGTH_SHORT).show();
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.notifyAppWidgetViewDataChanged(widgetId,R.id.widgetList);

        String timeString =
                DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date());

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.new_app_widget);
        views.setTextViewText(R.id.update_value, "Sist oppdatert: " + timeString);
        Toast.makeText(context, "Sist oppdatert: " + timeString, Toast.LENGTH_SHORT).show();
        appWidgetManager.partiallyUpdateAppWidget(widgetId, views);
        appWidgetManager.notifyAppWidgetViewDataChanged(widgetId,R.id.widgetList);
        //appWidgetManager.updateAppWidget(widgetId, views);
    }
}