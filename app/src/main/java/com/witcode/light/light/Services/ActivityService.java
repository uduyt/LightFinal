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
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.witcode.light.light.R;
import com.witcode.light.light.activities.MainActivity;
import com.witcode.light.light.backend.CheckIfValidListener;
import com.witcode.light.light.backend.GetNearRoutePoints;
import com.witcode.light.light.backend.NetworkChangeReceiver;
import com.witcode.light.light.backend.OnConnectivityChangeListener;
import com.witcode.light.light.backend.OnPointsCompleteListener;
import com.witcode.light.light.backend.ValidateMapPoint;
import com.witcode.light.light.domain.MapPoint;
import com.witcode.light.light.fragments.ActivityFragment;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import com.witcode.light.light.backend.OnTaskCompletedListener;
import com.witcode.light.light.backend.UpdateLights;

public class ActivityService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    private static final String LOG_TAG = "ForegroundService";
    private int seconds, minutes, hours;
    private Timer walkTimer;
    public static boolean IS_SERVICE_RUNNING = false;
    private Location oldLocation, mLastLocation;
    private double speed, distance, mLights, distanceDelta = 0;
    private GoogleApiClient mGoogleApiClient;
    private ServiceBinder mActualBinder = null;
    private String locationState;
    public MainActivity mActivity = null;
    public static int CURRENT_ACTIVITY = -1;
    public Service mService = this;
    private ArrayList<MapPoint> mUserRoutePoints;
    public static String LINE;
    public static boolean URBAN, CERCANIAS;
    BroadcastReceiver mbr;


    public class LocalBinder extends Binder {

        public ActivityService getService() {
            return ActivityService.this;
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
        walkTimer.scheduleAtFixedRate(new ActivityService.MyTimerTask(), 0, 1000);
        mGoogleApiClient.connect();
        mUserRoutePoints = new ArrayList<>();
        locationState = "Buscando señal GPS";

        mbr = new NetworkChangeReceiver(new OnConnectivityChangeListener() {
            @Override
            public void OnChange(final int connectivity) {
                if (mUserRoutePoints != null) {
                    for (MapPoint mp : mUserRoutePoints) {
                        if (mp.getValidated() == MapPoint.WAITING_VALIDATION) {


                            ValidateMapPoint(mp);


                        }
                    }
                }
            }
        });
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        this.registerReceiver(mbr, filter);
    }

    public void setBinder(ServiceBinder serviceBinder) {
        mActualBinder = serviceBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            if (intent.getAction().equals("start_foreground")) {


            } else if (intent.getAction().equals("action_kill")) {
                Log.v("mytag", "trying to kill service");
                KillService();
            } else if (intent.getAction().equals("bind_activity")) {

            }
        }

        return START_STICKY;
    }

    private void showNotification() {
        Intent iContentPress = new Intent(this, MainActivity.class);
        iContentPress.setAction("started_activity");
        iContentPress.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent piContent = PendingIntent.getActivity(this, 0,
                iContentPress, PendingIntent.FLAG_CANCEL_CURRENT);

        Intent iStopPress = new Intent(this, MainActivity.class);
        iStopPress.setAction("action_stop");
        iContentPress.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent piStop = PendingIntent.getActivity(this, 0,
                iStopPress, PendingIntent.FLAG_CANCEL_CURRENT);

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
        ActivityService.IS_SERVICE_RUNNING = false;
        walkTimer.cancel();
        seconds = 0;
        distance = 0;
        speed = 0;
        mLights = 0;

        if (mActivity != null) {
            mActivity.serviceBound = false;
        }

        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);
        mGoogleApiClient.disconnect();
        super.onDestroy();
        Log.i(LOG_TAG, "In onDestroy");

        this.unregisterReceiver(mbr);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(LOG_TAG, "Received Start Foreground Intent ");
        showNotification();
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

                        if (mLastLocation.getTime() - oldLocation.getTime() > 4000) { //Que hayan pasado minimo 4 segundos entre posiciones
                            if (mLastLocation.getAccuracy() < 40) {
                                if (mLastLocation.distanceTo(oldLocation) > (mLastLocation.getAccuracy() + oldLocation.getAccuracy())) {
                                    Log.v("location_update", "updating values");

                                    speed = (mLastLocation.distanceTo(oldLocation) / (mLastLocation.getTime() - oldLocation.getTime())) * 3600;


                                    distanceDelta = mLastLocation.distanceTo(oldLocation);
                                    distance += distanceDelta;
                                    oldLocation = mLastLocation;


                                    MapPoint mapPoint = new MapPoint();
                                    mapPoint.setLatLng(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
                                    mapPoint.setTime(mLastLocation.getTime());
                                    mapPoint.setType(MapPoint.USER_ROUTE);

                                    //formulas de lights
                                    if (CURRENT_ACTIVITY == ActivityFragment.ACTIVITY_WALK) {
                                        mapPoint.setLights((distanceDelta / 100) * Math.sqrt(speed / 6));
                                        mLights += mapPoint.getLights();
                                    } else if (CURRENT_ACTIVITY == ActivityFragment.ACTIVITY_BIKE) {
                                        mapPoint.setLights((distanceDelta / 500) * Math.sqrt(speed / 15));
                                        mLights += mapPoint.getLights();
                                    } else if (CURRENT_ACTIVITY == ActivityFragment.ACTIVITY_BUS)
                                        mapPoint.setLights((distanceDelta / 1300));

                                    else if (CURRENT_ACTIVITY == ActivityFragment.ACTIVITY_RAILROAD)
                                        mapPoint.setLights((distanceDelta / 1800) * Math.sqrt(speed / 15));

                                    else if (CURRENT_ACTIVITY == ActivityFragment.ACTIVITY_CARSHARE)
                                        mapPoint.setLights((distanceDelta / 2000) * Math.sqrt(speed / 15));

                                    mUserRoutePoints.add(mapPoint);

                                    if ((CURRENT_ACTIVITY == ActivityFragment.ACTIVITY_WALK && speed > 25) ||
                                            (CURRENT_ACTIVITY == ActivityFragment.ACTIVITY_BIKE && speed > 60)) {
                                        //Too fast
                                        Intent iContentPress = new Intent(mService, MainActivity.class);
                                        iContentPress.setAction("too_fast");
                                        mLights=0;
                                        iContentPress.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        PendingIntent piContent = PendingIntent.getActivity(mService, 0,
                                                iContentPress, PendingIntent.FLAG_CANCEL_CURRENT);

                                        try {
                                            piContent.send();
                                        } catch (PendingIntent.CanceledException e) {
                                            e.printStackTrace();
                                        }

                                    }

                                    if (CURRENT_ACTIVITY == ActivityFragment.ACTIVITY_BUS || CURRENT_ACTIVITY == ActivityFragment.ACTIVITY_RAILROAD) {
                                        //validar mi mapPoint
                                        if (CURRENT_ACTIVITY == ActivityFragment.ACTIVITY_RAILROAD) {
                                            URBAN = CERCANIAS;
                                        }
                                        ValidateMapPoint(mapPoint);
                                        Log.v("tagg", "loc:: lat: " + mapPoint.getLatLng().latitude + ", lon: " + mapPoint.getLatLng().longitude);


                                    }else if(CURRENT_ACTIVITY == ActivityFragment.ACTIVITY_WALK || CURRENT_ACTIVITY == ActivityFragment.ACTIVITY_BIKE){
                                        mapPoint.setValidated(MapPoint.VALIDATED);
                                    }

                                }
                            }
                        }

                        if (mLastLocation.getTime() - oldLocation.getTime() > 9000) { //Ver si han pasado mas de 9segs entre posiciones
                            locationState = "La señal GPS es débil";
                        } else {
                            locationState = "La señal GPS es buena";
                        }


                    } else {

                        if (mLastLocation.getAccuracy() < 40) {                    //primera posicion con buena precision
                            oldLocation = mLastLocation;
                            MapPoint mapPoint = new MapPoint();
                            mapPoint.setTime(mLastLocation.getTime());
                            mapPoint.setLatLng(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
                            mapPoint.setType(MapPoint.USER_ROUTE);

                            mUserRoutePoints.add(mapPoint);
                            locationState = "Buscando señal GPS";

                            ValidateMapPoint(mapPoint);
                        } else {
                            //primera posicion con mala precision
                            locationState = "La señal GPS es débil";
                        }
                    }
                } else { //mLastLocation is null
                    if (seconds > 10) {
                        locationState = "no hay señal GPS...";

                    }
                }

                Log.v("ForegroundService", "update with; seconds: " + seconds + ", distance: " + distance + ", speed: " + speed);
            }
            if (mActualBinder != null) {
                mActualBinder.OnGPSUpdate(locationState);
                mActualBinder.OnUpdate(distance, speed, seconds, mLights, mUserRoutePoints);
            }
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
        ActivityService.IS_SERVICE_RUNNING = false;
        stopSelf();
    }

    private void ValidateMapPoint(MapPoint mp){
        (new ValidateMapPoint(mService, LINE, CURRENT_ACTIVITY, URBAN, mp, new CheckIfValidListener() {
            @Override
            public void OnComplete(boolean valid, MapPoint mapPoint) {

                MapPoint mp;
                for (int i = 0; i < mUserRoutePoints.size(); i++) {
                    mp = mUserRoutePoints.get(i);
                    if (mp.getId().equals(mapPoint.getId())) {
                        if (valid && (i == 0 || mUserRoutePoints.get(i - 1).getValidated() != MapPoint.NOT_VALIDATED))
                            //es valido y el anterior no estaba fuera

                            if (i != 0 && mp.getTime() - mUserRoutePoints.get(i - 1).getTime() > 120000) {
                                //es valido pero ha pasado mucho tiempo entre el anterior y este
                                mp.setValidated(MapPoint.BIG_JUMP);
                                Log.v("tagg", "bigjump1: con i: " + i + ", mappoints: " + mUserRoutePoints.toString());
                            } else {
                                mp.setValidated(MapPoint.VALIDATED);
                                mLights += mp.getLights();
                            }


                        else if (valid && mUserRoutePoints.get(i - 1).getValidated() == MapPoint.NOT_VALIDATED) {
                            //es valido pero el anterior estaba fuera de camino
                            mp.setValidated(MapPoint.BIG_JUMP);

                            Log.v("tagg", "bigjump2: con i: " + i + ", mappoints: " + mUserRoutePoints.toString());
                        } else
                            //no es valido
                            mp.setValidated(MapPoint.NOT_VALIDATED);
                    }
                }
            }

            @Override
            public void OnError(String result, int resultCode, int resultType) {
                //todo gestionar sin conexion
            }
        })).execute();
    }
}