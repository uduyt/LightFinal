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
import android.graphics.Color;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.witcode.light.light.R;
import com.witcode.light.light.activities.MainActivity;
import com.witcode.light.light.backend.CheckIfValidListener;
import com.witcode.light.light.backend.GetInitialActivityPoints;
import com.witcode.light.light.backend.GetNearRoutePoints;
import com.witcode.light.light.backend.MyServerClass;
import com.witcode.light.light.backend.NetworkChangeReceiver;
import com.witcode.light.light.backend.OnConnectivityChangeListener;
import com.witcode.light.light.backend.OnInitialPointsCompleteListener;
import com.witcode.light.light.backend.OnPointsCompleteListener;
import com.witcode.light.light.backend.RetryTask;
import com.witcode.light.light.backend.ValidateMapPoint;
import com.witcode.light.light.domain.ActivityObject;
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
    private Location oldLocation, mLastLocation;
    private double distanceDelta = 0;
    private GoogleApiClient mGoogleApiClient;
    private ServiceBinder mActualBinder = null;
    public MainActivity mActivity = null;
    public Service mService = this;
    private ArrayList<RetryTask> mAsyncTasks=new ArrayList<>();
    public static ActivityObject ActivityObject=new ActivityObject();
    private Timer mTimer;
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
        mGoogleApiClient.connect();



        mTimer = new Timer();
        mTimer.scheduleAtFixedRate(new ActivityService.MyTimerTask(), 0, 3000);

        //TODO ver que hacer con esto
        mbr = new NetworkChangeReceiver(new OnConnectivityChangeListener() {
            @Override
            public void OnChange(final int connectivity) {
                if (ActivityObject.getUserRoutePoints() != null) {
                    for (MapPoint mp : ActivityObject.getUserRoutePoints()) {
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
        GetInitialActivityPoints();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            if (intent.getAction().equals("start_foreground")) {


            } else if (intent.getAction().equals("action_kill")) {
                Log.v("mytag", "ActivityService: trying to kill service");
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
                .setTicker("Actividad en proceso")
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
        ActivityService.ActivityObject.setRunning(false);

        mTimer.cancel();
        if (mActivity != null) {
            //TODO habria que revisarlo
            mActivity.serviceBound = false;
        }

        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();

        Log.v("mytag", "ActivityService: In onDestroy");

        try{
            this.unregisterReceiver(mbr);
        }catch (IllegalArgumentException e){
            e.printStackTrace();
        }

        ActivityObject=new ActivityObject();

        super.onDestroy();
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

            if (mLastLocation != null) {
                if (oldLocation != null) {
                    if (mLastLocation.getTime() - oldLocation.getTime() > 4000) {
                        //Que hayan pasado minimo 4 segundos entre posiciones
                        if (mLastLocation.getAccuracy() < 40) {
                            //Que haya un mínimo de precisión
                            if (mLastLocation.distanceTo(oldLocation) > (mLastLocation.getAccuracy() + oldLocation.getAccuracy())) {

                                Log.v("location_update", "updating values");

                                ActivityObject.setSpeed((mLastLocation.distanceTo(oldLocation) / (mLastLocation.getTime() - oldLocation.getTime())) * 3600);


                                distanceDelta = mLastLocation.distanceTo(oldLocation);
                                ActivityObject.addToDistance(distanceDelta);



                                MapPoint mapPoint = new MapPoint();
                                mapPoint.setLatLng(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
                                mapPoint.setTime(mLastLocation.getTime());
                                mapPoint.setType(MapPoint.USER_ROUTE);


                                //formulas de lights
                                if (ActivityObject.getType() == ActivityFragment.ACTIVITY_WALK) {
                                    mapPoint.setLights((distanceDelta / 100) * Math.sqrt(ActivityObject.getSpeed() / 6));
                                    ActivityObject.addToLights(mapPoint.getLights());
                                } else if (ActivityObject.getType() == ActivityFragment.ACTIVITY_BIKE) {
                                    mapPoint.setLights((distanceDelta / 500) * Math.sqrt(ActivityObject.getSpeed() / 15));
                                    ActivityObject.addToLights(mapPoint.getLights());
                                } else if (ActivityObject.getType() == ActivityFragment.ACTIVITY_BUS)
                                    mapPoint.setLights((distanceDelta / 1300));

                                else if (ActivityObject.getType() == ActivityFragment.ACTIVITY_RAILROAD)
                                    mapPoint.setLights((distanceDelta / 1800) * Math.sqrt(ActivityObject.getSpeed() / 15));

                                else if (ActivityObject.getType() == ActivityFragment.ACTIVITY_CARSHARE)
                                    mapPoint.setLights((distanceDelta / 2000) * Math.sqrt(ActivityObject.getSpeed() / 15));

                                ActivityObject.addRouteMapPoint(mapPoint);




                                if ((ActivityObject.getType() == ActivityFragment.ACTIVITY_WALK && ActivityObject.getSpeed() > 25) ||
                                        (ActivityObject.getType() == ActivityFragment.ACTIVITY_BIKE && ActivityObject.getSpeed() > 60)) {
                                    //Too fast
                                    Intent iContentPress = new Intent(mService, MainActivity.class);
                                    iContentPress.setAction("too_fast");
                                    ActivityObject.setLights(0);
                                    ActivityObject.setTooFast(true);
                                    iContentPress.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    PendingIntent piContent = PendingIntent.getActivity(mService, 0,
                                            iContentPress, PendingIntent.FLAG_CANCEL_CURRENT);

                                    try {
                                        piContent.send();
                                    } catch (PendingIntent.CanceledException e) {
                                        e.printStackTrace();
                                    }

                                }

                                if (ActivityObject.getType() == ActivityFragment.ACTIVITY_BUS || ActivityObject.getType() == ActivityFragment.ACTIVITY_RAILROAD) {
                                    //validar mi mapPoint
                                    ValidateMapPoint(mapPoint);
                                } else if (ActivityObject.getType() == ActivityFragment.ACTIVITY_WALK || ActivityObject.getType() == ActivityFragment.ACTIVITY_BIKE) {
                                    mapPoint.setValidated(MapPoint.VALIDATED);
                                }

                                PolylineOptions pOptions = new PolylineOptions();
                                pOptions.add(new LatLng(oldLocation.getLatitude(), oldLocation.getLongitude()));
                                pOptions.add(mapPoint.getLatLng());
                                pOptions.color(getColorFromMapPoint(mapPoint));
                                pOptions.zIndex(2);

                                ActivityObject.addRoutePolylineOption(pOptions);

                                SendNewMapPointToUi(pOptions);

                                oldLocation = mLastLocation;
                            }
                        }
                    }

                    if (mLastLocation.getTime() - oldLocation.getTime() > 9000) { //Ver si han pasado mas de 9segs entre posiciones
                        SendGPSUpdate("La señal GPS es débil");
                    } else {
                        SendGPSUpdate("La señal GPS es buena");
                    }


                } else {
                    //old location is null

                    if (mLastLocation.getAccuracy() < 40) { //primera posicion con buena precision
                        oldLocation = mLastLocation;
                        MapPoint mapPoint = new MapPoint();
                        mapPoint.setTime(mLastLocation.getTime());
                        mapPoint.setLatLng(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
                        mapPoint.setType(MapPoint.USER_ROUTE);

                        ActivityObject.addRouteMapPoint(mapPoint);
                        SendGPSUpdate("Buscando señal GPS");

                        ValidateMapPoint(mapPoint);
                    } else {
                        //primera posicion con mala precision
                        SendGPSUpdate("La señal GPS es débil");
                    }
                }
            } else {
                //mLastLocation is null
                if (ActivityObject.getSeconds() > 10) {
                    SendGPSUpdate("No hay señal GPS");

                }
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
        ActivityService.ActivityObject.setRunning(false);
        stopSelf();
    }

    private void SendGPSUpdate(String update){
        if(mActualBinder!=null){
            mActualBinder.OnGPSUpdate(update);
        }
    }

    private void ValidateMapPoint(final MapPoint mp) {

        (new ValidateMapPoint(mService, ActivityObject.getLine(), ActivityObject.getType(), ActivityObject.isCercaniasOrUrban(), mp, new CheckIfValidListener() {
            @Override
            public void OnComplete(boolean valid, MapPoint mapPoint) {

                MapPoint mp;
                for (int i = 0; i < ActivityObject.getUserRoutePoints().size(); i++) {
                    mp = ActivityObject.getUserRoutePoints().get(i);
                    if (mp.getId().equals(mapPoint.getId())) {
                        if (valid && (i == 0 || ActivityObject.getUserRoutePoints().get(i - 1).getValidated() != MapPoint.NOT_VALIDATED))
                            //es valido y el anterior no estaba fuera

                            if (i != 0 && mp.getTime() - ActivityObject.getUserRoutePoints().get(i - 1).getTime() > 120000) {
                                //es valido pero ha pasado mucho tiempo entre el anterior y este
                                mp.setValidated(MapPoint.BIG_JUMP);
                            } else {
                                mp.setValidated(MapPoint.VALIDATED);
                                ActivityObject.addToLights(mp.getLights());
                            }


                        else if (valid && ActivityObject.getUserRoutePoints().get(i - 1).getValidated() == MapPoint.NOT_VALIDATED) {
                            //es valido pero el anterior estaba fuera de camino
                            mp.setValidated(MapPoint.BIG_JUMP);
                        } else {
                            //no es valido
                            mp.setValidated(MapPoint.NOT_VALIDATED);
                        }

                        if(i>0){
                            ActivityObject.getUserRoutePolylines().get(i - 1).color(getColorFromMapPoint(mp));
                            SendUpdatedPolylineToUi(i - 1, mp);
                        }

                        if(mAsyncTasks.size()>0){
                            RetryTask serverClass=mAsyncTasks.get(mAsyncTasks.size()-1);
                            mAsyncTasks.remove(mAsyncTasks.size()-1);
                            serverClass.runTask();
                        }

                    }
                }
            }

            @Override
            public void OnError(String result, int resultCode, int resultType) {
                if(resultCode==MyServerClass.NOT_CONNECTED){
                    mAsyncTasks.add(new ValidateMapPoint(mService,ActivityObject.getLine(), ActivityObject.getType(), ActivityObject.isCercaniasOrUrban(), mp, this));
                }
            }
        })).execute();
    }

    private void SendNewMapPointToUi(PolylineOptions polylineOptions) {
        if (mActualBinder != null) {
            mActualBinder.OnNewMapPoint(ActivityObject.getDistance(), ActivityObject.getSpeed(), polylineOptions);
            Log.v("mytag", "ActivityService: sent new mappoint to ui");
        }
    }

    private void SendUpdatedPolylineToUi(int index, MapPoint mapPoint) {
        if (mActualBinder != null) {
            mActualBinder.OnPolylineUpdate(index
                    , getColorFromMapPoint(mapPoint));
        }
    }

    private int getColorFromMapPoint(MapPoint mp) {
        int color;

        switch (mp.getValidated()) {
            case MapPoint.VALIDATED:
                color = Color.parseColor("#00ff00");
                break;
            case MapPoint.NOT_VALIDATED:
            case MapPoint.BIG_JUMP:
                color = Color.parseColor("#ff0000");
                break;
            default:
                color = Color.parseColor("#0000ff");
                break;
        }

        return color;
    }

    private void GetInitialActivityPoints(){

        if(ActivityObject.getType()==ActivityFragment.ACTIVITY_BUS||ActivityObject.getType()==ActivityFragment.ACTIVITY_RAILROAD){
            Log.v("mytag","ActivityService: on initialactivitypoints method");
            (new GetNearRoutePoints(this, ActivityObject.isCercaniasOrUrban(), ActivityObject.getType(), ActivityObject.getLine(), new OnPointsCompleteListener() {
                @Override
                public void OnComplete(ArrayList<ArrayList<MapPoint>> points) {
                    Log.v("mytag","ActivityService: initial points: " + points);
                    PolylineOptions pOptions = new PolylineOptions();
                    for (ArrayList<MapPoint> mps : points) {
                        pOptions = new PolylineOptions();
                        for (MapPoint mp : mps) {
                            pOptions.add(mp.getLatLng());
                        }

                        pOptions.color(Color.parseColor("#000000"));
                        pOptions.zIndex(1);
                        ActivityObject.addToInitialRoutePolylines(pOptions);
                    }

                    if(mActualBinder!=null){
                        mActualBinder.OnInitialPointsArrived(ActivityObject.getInitialRoutePolylines());
                        Log.v("mytag","ActivityService: on initialactivitypoints sent de verdad");
                    }
                }

                @Override
                public void OnError(String result, int resultCode, int resultType) {
                    if(resultCode==MyServerClass.NOT_CONNECTED){
                        mAsyncTasks.add(new GetNearRoutePoints(mService, ActivityObject.isCercaniasOrUrban(), ActivityObject.getType(), ActivityObject.getLine(), this));
                    }

                }
            })).execute();

        }
    }
}