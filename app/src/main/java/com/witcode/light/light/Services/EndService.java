package com.witcode.light.light.Services;

/**
 * Created by carlo on 02/04/2017.
 */

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.witcode.light.light.Utils;
import com.witcode.light.light.activities.MainActivity;
import com.witcode.light.light.backend.AddActivityHistory;
import com.witcode.light.light.backend.CheckIfValidListener;
import com.witcode.light.light.backend.MyServerClass;
import com.witcode.light.light.backend.NetworkChangeReceiver;
import com.witcode.light.light.backend.OnConnectivityChangeListener;
import com.witcode.light.light.backend.OnTaskCompletedListener;
import com.witcode.light.light.backend.UpdateLights;
import com.witcode.light.light.backend.ValidateMapPoint;
import com.witcode.light.light.domain.ActivityObject;
import com.witcode.light.light.domain.MapPoint;
import com.witcode.light.light.fragments.ActivityFragment;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class EndService extends Service {
    private Timer Timer;
    private boolean isLightsUpdated=false;
    private EndServiceBinder mActualBinder=null;
    private MainActivity mActivity;
    private ServiceConnection mConnection;
    private ActivityObject mActivityObject=new ActivityObject();
    private boolean fragmentAlive=true;
    private EndService mService=this;
    public static boolean IS_SERVICE_RUNNING = false;


    public class LocalBinder extends Binder {

        public EndService getService() {
            return EndService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        IS_SERVICE_RUNNING = true;

        Timer = new Timer();

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            IS_SERVICE_RUNNING = true;
        }

        Log.v("mytag", "EndService: onstartcommand");

        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.v("mytag", "EndService: onbind");
        return mBinder;
    }
    private LocalBinder mBinder = new LocalBinder();




    private class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            // Do stuff
            Log.v("mytag", "EndService: Timer run and to validate");
            Validate();

        }
    }

    public void setData(ActivityObject activityObject, ServiceConnection connection, MainActivity mainActivity, EndServiceBinder binder){
        Log.v("mytag", "EndService: on setData and lights= " + activityObject.getLights());

        mActivityObject=activityObject;
        mActualBinder=binder;
        mConnection=connection;
        mActivity=mainActivity;
        Timer.scheduleAtFixedRate(new EndService.MyTimerTask(), 0, 8000);
        Log.v("mytag", "EndService: to validate2");
        Validate();

    }


    private void Validate(){
        if(isValidationFinished()){
            Log.v("mytag", "EndService: lights1= " + mActivityObject.getLights());
            Log.v("mytag", "EndService: validate: validation finished");
            UpdateLights();
        }else if(MyServerClass.isConnected(this)){
            Log.v("mytag", "EndService: lights2= " + mActivityObject.getLights());
            Log.v("mytag", "EndService: validate: validation not finished and connected");
            for (MapPoint mp : mActivityObject.getUserRoutePoints()) {
                Log.v("mytag", "EndService: lights3= " + mActivityObject.getLights());
                if (mp.getValidated() == MapPoint.WAITING_VALIDATION) {
                    ValidateMapPoint(mp);
                }
            }
        }else{
            Log.v("mytag", "EndService: validate: validation not finished and not connected");
        }
    }

    private boolean isValidationFinished(){
        boolean finished=true;
        for (MapPoint mp : mActivityObject.getUserRoutePoints()) {
            if (mp.getValidated() == MapPoint.WAITING_VALIDATION) {
                finished=false;
            }
        }
        return finished;
    }

    private void ValidateMapPoint(final MapPoint mapPoint){

        Log.v("mytag", "EndService: lights4= " + mActivityObject.getLights());
        (new ValidateMapPoint(this, mActivityObject.getLine(), mActivityObject.getType(), mActivityObject.isCercaniasOrUrban(), mapPoint, new CheckIfValidListener() {
            @Override
            public void OnComplete(boolean valid, MapPoint mapPoint) {

                if(mapPoint.getValidated()==MapPoint.WAITING_VALIDATION){
                    Log.v("mytag", "EndService: MapPoint Validated");
                    MapPoint mp;
                    Log.v("mytag", "EndService: lights5= " + mActivityObject.getLights());
                    for (int i = 0; i < mActivityObject.getUserRoutePoints().size(); i++) {
                        mp = mActivityObject.getUserRoutePoints().get(i);
                        if (mp.getId().equals(mapPoint.getId())) {
                            if (valid && (i == 0 || mActivityObject.getUserRoutePoints().get(i - 1).getValidated() != MapPoint.NOT_VALIDATED))
                                //es valido y el anterior no estaba fuera

                                if (i != 0 && mp.getTime() - mActivityObject.getUserRoutePoints().get(i - 1).getTime() > 120000) {
                                    //es valido pero ha pasado mucho tiempo entre el anterior y este
                                    mp.setValidated(MapPoint.BIG_JUMP);
                                } else {
                                    mp.setValidated(MapPoint.VALIDATED);
                                    mActivityObject.addToLights(mp.getLights());
                                }


                            else if (valid && mActivityObject.getUserRoutePoints().get(i - 1).getValidated() == MapPoint.NOT_VALIDATED) {
                                //es valido pero el anterior estaba fuera de camino
                                mp.setValidated(MapPoint.BIG_JUMP);
                            } else {
                                //no es valido
                                mp.setValidated(MapPoint.NOT_VALIDATED);
                            }
                        }
                    }
                }else{
                    Log.v("mytag", "EndService: MapPoint was already validated");
                }

            }

            @Override
            public void OnError(String result, int resultCode, int resultType) {
                Log.v("mytag", "EndService: MapPoint not Validated");
            }
        })).execute();
    }

    private void UpdateLights(){
        Log.v("mytag", "EndService: In UpdateLights method");
        if(!isLightsUpdated){
            Log.v("mytag", "EndService: updateLights: isLightsUpdated=false");
            isLightsUpdated=true;
            Log.v("mytag", "EndService: lights: " + mActivityObject.getLights() + ", before floored");
            final int lights=(int) Math.floor(mActivityObject.getLights());
            Log.v("mytag", "EndService: lights: " + mActivityObject.getLights() + ", floored: " + lights);
            (new UpdateLights(this, lights, Utils.getUpdateLightsActionTypeFromActivityActionType(mActivityObject.getType()), new OnTaskCompletedListener() {
                @Override
                public void OnComplete(String result, int resultCode, int resultType) {
                    if(resultCode==MyServerClass.SUCCESSFUL && result.equals("ok")){
                        Log.v("mytag", "EndService: updateLights onComplete and successful");
                        if(fragmentAlive){
                            Log.v("mytag", "EndService: updating lights and fragment is alive");
                            mActualBinder.OnServiceEnded(String.valueOf(lights));
                        }else{
                            Log.v("mytag", "EndService: updating lights and fragment is dead");
                            Utils.sendNotification(mService, "Tu actividad se ha validado", "enhorabuena, has ganado " + String.valueOf(lights) + " lights",null);
                        }

                        (new AddActivityHistory(mService, String.valueOf(
                                mActivityObject.getLights()),
                                String.format(Locale.ENGLISH, "%.2f", ((float) mActivityObject.getDistance() / 1000) / ((float) mActivityObject.getSeconds() / 3600)),
                                String.valueOf(mActivityObject.getSeconds()),
                                String.valueOf(mActivityObject.getDistance()),
                                mActivityObject.getTypeString(),

                                new OnTaskCompletedListener() {
                            @Override
                            public void OnComplete(String result, int resultCode, int resultType) {

                            }
                        })).execute();

                        DestroySelf();
                    }else{
                        isLightsUpdated=false;
                        Log.v("mytag", "EndService: error updating lights");
                    }
                }
            })).execute();
        }
    }

    private void DestroySelf(){
        Log.v("mytag", "EndService: destroying self");
        Timer.cancel();
        IS_SERVICE_RUNNING = false;
        mActivity.unbindService(mConnection);
        stopSelf();
    }



    public boolean isFragmentAlive() {
        return fragmentAlive;
    }

    public void setFragmentAlive(boolean fragmentAlive) {
        this.fragmentAlive = fragmentAlive;
    }
}