package com.example.sx.practicalassistant.view.appwidget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.widget.RemoteViews;

import com.example.sx.practicalassistant.R;
import com.example.sx.practicalassistant.service.BootReceiver;
import com.example.sx.practicalassistant.service.VoiceService;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.ALARM_SERVICE;

/**
 * Created by SX on 2017/3/3.
 */

public class VoiceWidget extends AppWidgetProvider {
    private static Timer myTimer;
    private static int index = 0;
    private SharedPreferences preferences;
    private SharedPreferences.Editor edit;
    private PendingIntent sender;
    private Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("TEST", "onReceive: " );
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.e("TEST", "onUpdate: "+index );
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        Intent intent =new Intent(context, BootReceiver.class);
        intent.setAction("com.example.sx.practicalassistant.keepalive");
        context.sendBroadcast(intent);
        index++;
    }

    @Override
    public void onEnabled(Context context) {
        Log.e("TEST", "onEnabled: " );
        super.onEnabled(context);
        this.context = context;
        preferences = context.getSharedPreferences("practicalassistant", Context.MODE_PRIVATE);
        edit = preferences.edit();
        Intent intent =new Intent(context, BootReceiver.class);
        intent.setAction("com.example.sx.practicalassistant.keepalive");
        context.sendBroadcast(intent);
        sender= PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar=Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.SECOND, 0);

        AlarmManager alarm=(AlarmManager)context.getSystemService(ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(),1000*5,sender);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        Log.e("TEST", "onDeleted: "+index );
        super.onDeleted(context, appWidgetIds);
        index--;
        if(index<=0&&sender!=null){
            AlarmManager alarm=(AlarmManager)context.getSystemService(ALARM_SERVICE);
            alarm.cancel(sender);
            Log.e("TEST", "onDeleted:  alarm.cancel" );
        }
        context.stopService(new Intent(context, VoiceService.class));
    }


}
