package com.witcode.light.light.Services;

/**
 * Created by carlo on 02/04/2017.
 */

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.witcode.light.light.R;
import com.witcode.light.light.activities.MainActivity;
import com.witcode.light.light.backend.CheckIfValidListener;
import com.witcode.light.light.backend.MyServerClass;
import com.witcode.light.light.backend.NetworkChangeReceiver;
import com.witcode.light.light.backend.OnConnectivityChangeListener;
import com.witcode.light.light.backend.RetryTask;
import com.witcode.light.light.backend.ValidateMapPoint;
import com.witcode.light.light.domain.MapPoint;
import com.witcode.light.light.fragments.ActivityFragment;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class RetryService extends Service {
    private static ArrayList<RetryTask> mTasks = new ArrayList<>();
    private NetworkChangeReceiver mbr;
    private Timer RetryTimer;
    public static boolean IS_SERVICE_RUNNING = false;


    public class LocalBinder extends Binder {

        public RetryService getService() {
            return RetryService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mbr = new NetworkChangeReceiver(new OnConnectivityChangeListener() {
            @Override
            public void OnChange(int connectivity) {
                Log.v("mytag", "RetryService: connectivity changed");
                RunTasks();

                Log.v("mytag", "RetryService: tasks: " + mTasks.toString());
            }
        });
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        this.registerReceiver(mbr, filter);

        IS_SERVICE_RUNNING = true;

        RetryTimer = new Timer();
        RetryTimer.scheduleAtFixedRate(new RetryService.MyTimerTask(), 0, 10000);
    }

    public static void RunTasks() {
        Log.v("mytag", "RetryService: RUNTASKS: " + mTasks.toString());
        for(int i=0;i<mTasks.size();){
            mTasks.get(0).runTask();
            mTasks.remove(0);
            Log.v("mytag", "RetryService: tasks: " + mTasks.toString() + ", size= " + mTasks.size());
        }

    }


    public static void AddTask(RetryTask task) {
        mTasks.add(task);

        Log.v("mytag", "RetryService: addtask tasks: " + mTasks.toString());
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            IS_SERVICE_RUNNING = true;
        }

        Log.v("mytag", "RetryService: onstartcommand");

        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.v("mytag", "RetryService: onbind");
        return mBinder;
    }
    private LocalBinder mBinder = new LocalBinder();

    @Override
    public void onDestroy() {

        this.unregisterReceiver(mbr);
        RetryTimer.cancel();
        IS_SERVICE_RUNNING = false;
    }


    private class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            // Do stuff
            Log.v("mytag", "RetryService: timer task running");
            if(mTasks!=null && mTasks.size()>0)
                RunTasks();

        }
    }
}