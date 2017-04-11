package com.witcode.light.light;

/**
 * Created by carlo on 02/04/2017.
 */

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.Timer;
import java.util.TimerTask;

import backend.OnLocationUpdateListener;
import backend.ServiceBinder;

public class WalkService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {
    private static final String LOG_TAG = "ForegroundService";
    private int seconds, minutes, hours;
    private Timer walkTimer;
    public static boolean IS_SERVICE_RUNNING = false;
    private Location oldLocation, mLastLocation;
    private double speed, distance, mLights;
    private GoogleApiClient mGoogleApiClient;
    private ServiceBinder mActualBinder = null;
    private String currentActivity, locationState;
    public MainActivity mActivity=null;

    public class LocalBinder extends Binder {
        WalkService getService() {
            return WalkService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (mGoogleApiClient == null) {
            this.mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        walkTimer = new Timer();
        walkTimer.scheduleAtFixedRate(new WalkService.MyTimerTask(), 0, 1000);


    }

    public void setBinder(ServiceBinder serviceBinder) {
        mActualBinder = serviceBinder;
    }

    public int getSeconds() {
        return seconds;
    }

    ;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            if (intent.getAction().equals("start_foreground")) {
                Log.i(LOG_TAG, "Received Start Foreground Intent ");
                showNotification();
                Toast.makeText(this, "Service Started!", Toast.LENGTH_SHORT).show();

            } else if (intent.getAction().equals("action_stop")) {
                Log.i(LOG_TAG, "Clicked Stop");

                Toast.makeText(this, "Clicked Stop!", Toast.LENGTH_SHORT)
                        .show();
                stopForeground(true);

                stopSelf();

            }
        }

        return START_STICKY;
    }

    private void showNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction("action_main");
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Intent nextIntent = new Intent(this, WalkService.class);
        nextIntent.setAction("action_stop");
        PendingIntent stopIndent = PendingIntent.getService(this, 0,
                nextIntent, 0);

        Bitmap icon = BitmapFactory.decodeResource(getResources(),
                R.mipmap.ic_icono_app);

        RemoteViews rvs = new RemoteViews(getPackageName(), R.layout.custom_walk_notif);
        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("Actividad en proceso")
                .setTicker("Esto es un ticker")
                .setContent(rvs)
                .setSmallIcon(R.mipmap.ic_icono_app)
                .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .addAction(R.drawable.ic_stop_black_24dp, "Terminar actividad",
                        stopIndent).build();
        startForeground(1,
                notification);

    }

    @Override
    public void onDestroy() {
        WalkService.IS_SERVICE_RUNNING = false;
        walkTimer.cancel();
        seconds=0;
        distance=0;
        speed=0;
        mLights=0;
        if(mActivity!=null)
        mActivity.UpdateFabActivity();
        super.onDestroy();
        Log.i(LOG_TAG, "In onDestroy");
        Toast.makeText(this, "Service Detroyed!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();

    private class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            // Do stuff

            seconds++;
            minutes = (seconds) / 60;
            hours = minutes / 60;


            if (seconds % 3 == 1) {
                if (mLastLocation != null) {


                    if (oldLocation != null) {

                        if (mLastLocation.getTime() - oldLocation.getTime() > 4000) {
                            if (mLastLocation.getAccuracy() < 30) {
                                if (mLastLocation.distanceTo(oldLocation) > (mLastLocation.getAccuracy() + oldLocation.getAccuracy())) {
                                    Log.v("location_update", "adding values");

                                    speed = (mLastLocation.distanceTo(oldLocation) / (mLastLocation.getTime() - oldLocation.getTime())) * 360;
                                    distance += mLastLocation.distanceTo(oldLocation);
                                    oldLocation = mLastLocation;
                                    if (currentActivity.equals("walk"))
                                        mLights += (distance / 100) * Math.sqrt(speed / 6) * 0.7;
                                    else
                                        mLights += (distance / 500) * Math.sqrt(speed / 15) * 0.7;

                                    if (currentActivity.equals("walk") & speed > 16) {
                                        //Too fast

                                    } else if (currentActivity.equals("bike") & speed > 60) {
                                        //Too fast

                                    }
                                }
                            }
                        }

                        if (mLastLocation.getTime() - oldLocation.getTime() > 10000) {
                            locationState = "La señal GPS es débil";
                        } else {
                            locationState = "La señal GPS es buena";
                        }


                    } else {
                        locationState = "Buscando señal GPS...";

                        if (mLastLocation.getAccuracy() < 30)
                            oldLocation = mLastLocation;

                    }
                }

                Log.v("ForegroundService", "update with; seconds: " + seconds + ", distance: " + distance + ", speed: " + speed);
            }
            if (mActualBinder != null)
                mActualBinder.OnUpdate(String.valueOf(distance), String.valueOf(speed), seconds, String.valueOf(mLights), locationState);
        }
    }

    public LocationRequest createLocationRequest(int accuracy) {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(accuracy);
        return mLocationRequest;
    }

    public void setLocation(Location location) {
        if (location != null)
            mLastLocation = location;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                    createLocationRequest(LocationRequest.PRIORITY_HIGH_ACCURACY), this); // This is the changed line.
        } catch (SecurityException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        setLocation(location);
    }

    public void KillService() {
        stopForeground(true);
        WalkService.IS_SERVICE_RUNNING = false;
        stopSelf();
    }
}