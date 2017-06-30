package com.witcode.light.light.Services;

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
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.witcode.light.light.R;
import com.witcode.light.light.activities.MainActivity;
import com.witcode.light.light.fragments.StartFragment;

import java.util.Timer;
import java.util.TimerTask;

import com.witcode.light.light.backend.OnTaskCompletedListener;
import com.witcode.light.light.backend.UpdateLights;

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
    private String locationState;
    public MainActivity mActivity = null;
    public static int CURRENT_ACTIVITY = -1;
    public Service mService = this;


    public class LocalBinder extends Binder {

        public WalkService getService() {
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
        mGoogleApiClient.connect();

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

            } else if (intent.getAction().equals("action_stop")) {
                Log.i(LOG_TAG, "Clicked Stop");

                new UpdateLights(mService,(int) Math.round(mLights), new OnTaskCompletedListener() {
                    @Override
                    public void OnComplete(String result, int resultCode, int resultType) {
                        Toast.makeText(mService, "Se ha terminado la acción, has ganado " + (int) mLights + " lights", Toast.LENGTH_SHORT)
                                .show();
                    }
                }).execute();

                KillService();

            }else if(intent.getAction().equals("action_kill")){
                Log.v("mytag", "trying to kill service");
                KillService();
            }
        }

        return START_STICKY;
    }

    private void showNotification() {
        Intent iContentPress = new Intent(this, MainActivity.class);
        iContentPress.setAction("action_main");
        iContentPress.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent piContent = PendingIntent.getActivity(this, 0,
                iContentPress, 0);

        Intent iStopPress = new Intent(this, WalkService.class);
        iStopPress.setAction("action_stop");
        PendingIntent piStop = PendingIntent.getService(this, 0,
                iStopPress, 0);

        Bitmap icon = BitmapFactory.decodeResource(getResources(),
                R.mipmap.ic_icono_app);

        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("Actividad en proceso")
                .setTicker("Esto es un ticker")
                .setSmallIcon(R.drawable.ic_bulb)
                .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setContentIntent(piContent)
                .setOngoing(true)
                .addAction(R.drawable.ic_stop_black_24dp, "Terminar actividad", piStop)
                .build();

        startForeground(1, notification);

    }

    @Override
    public void onDestroy() {
        WalkService.IS_SERVICE_RUNNING = false;
        walkTimer.cancel();
        seconds = 0;
        distance = 0;
        speed = 0;
        mLights = 0;
        if (mActivity != null) {
            mActivity.UpdateFabActivity();
            mActivity.serviceBound = false;
        }

        super.onDestroy();
        Log.i(LOG_TAG, "In onDestroy");
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

                                    speed = (mLastLocation.distanceTo(oldLocation) / (mLastLocation.getTime() - oldLocation.getTime())) * 3600;
                                    distance += mLastLocation.distanceTo(oldLocation);
                                    oldLocation = mLastLocation;
                                    if (CURRENT_ACTIVITY == StartFragment.ACTIVITY_WALK)
                                        mLights += (distance / 100) * Math.sqrt(speed / 6) * 0.7;
                                    else if (CURRENT_ACTIVITY == StartFragment.ACTIVITY_BIKE)
                                        mLights += (distance / 500) * Math.sqrt(speed / 15) * 0.7;

                                    if ((CURRENT_ACTIVITY == StartFragment.ACTIVITY_WALK && speed > 16) || (CURRENT_ACTIVITY == StartFragment.ACTIVITY_BIKE && speed > 60)) {
                                        //Too fast
                                        Intent iContentPress = new Intent(mService, MainActivity.class);
                                        iContentPress.setAction("too_fast");
                                        iContentPress.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        PendingIntent piContent = PendingIntent.getActivity(mService, 0,
                                                iContentPress, 0);

                                        try {
                                            piContent.send();
                                        } catch (PendingIntent.CanceledException e) {
                                            e.printStackTrace();
                                        }

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
        if (mActivity != null) {
            mActivity.UpdateFabActivity();
            mActivity.doUnBindService();
        }
    }
}