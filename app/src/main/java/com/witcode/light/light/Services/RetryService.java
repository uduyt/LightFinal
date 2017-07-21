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
    private ArrayList<RetryTask> mTasks = new ArrayList<>();
    private NetworkChangeReceiver mbr;
    public static boolean IS_SERVICE_RUNNING=false;


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
                Log.v("tagg","retry service connectivity changed");
                for (RetryTask task : mTasks) {
                    if (task.isSuccessful()) {
                        Log.v("tagg","retry service task not successful");
                        mTasks.remove(task);
                    } else {
                        task.runTask();
                    }

                }

                Log.v("tagg","retry service tasks: " + mTasks.toString());
            }
        });
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        this.registerReceiver(mbr, filter);

        IS_SERVICE_RUNNING=true;
    }


    public void AddTask(RetryTask task) {
        if (!mTasks.contains(task))
            mTasks.add(task);

        Log.v("tagg","retry service tasks: " + mTasks.toString());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            IS_SERVICE_RUNNING=true;
        }

        Log.v("tagg","retry service onstartcommand");

        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.v("tagg","retry service onbind");
        return mBinder;

    }

    private LocalBinder mBinder= new LocalBinder();
    @Override
    public void onDestroy() {

        this.unregisterReceiver(mbr);
        IS_SERVICE_RUNNING=false;
    }

}