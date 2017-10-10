package com.witcode.light.light.backend;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;

import com.witcode.light.light.R;
import com.witcode.light.light.Services.ActivityService;
import com.witcode.light.light.activities.MainActivity;
import com.witcode.light.light.domain.MapPoint;
import com.witcode.light.light.fragments.ActivityFragment;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by rosety on 10/7/17.
 */

public class EndActivityTask implements RetryTask {
    private int mLights;
    private ArrayList<MapPoint> mUserPoints;
    private boolean lightsSent = false;
    private Context mContext;
    private OnTaskUpdateListener mListener;
    private int mActivityType;
    private String mLine;
    private boolean mUrbanCercanias;
    private String actionType;

    public static final int SUCCESSFUL = 1;
    public static final int NOT_CONNECTED = 2;
    public static final int NOT_VALIDATED = 3;

    public EndActivityTask(Context c, int activityType, String line, boolean urban_cercanias, ArrayList<MapPoint> userPoints, String lights, OnTaskUpdateListener listener) {
        mContext = c;
        mUserPoints = userPoints;
        mListener = listener;
        mLights = (int) Math.round(Double.valueOf(lights));
        mActivityType = activityType;
        mLine = line;
        mUrbanCercanias = urban_cercanias;
    }


    @Override
    public void runTask() {
        Log.v("tagg", "end task ran");
        for (MapPoint mp : mUserPoints) {
            if (mp.getValidated() == MapPoint.WAITING_VALIDATION) {

                Log.v("tagg", "end task map point not validated: " + mUserPoints.toString());
                (new ValidateMapPoint(mContext, mLine, mActivityType, mUrbanCercanias, mp, new CheckIfValidListener() {
                    @Override
                    public void OnComplete(boolean valid, MapPoint mapPoint) {

                        Log.v("tagg", "end task map point came with valid(" + valid + ")");
                        MapPoint mp;
                        for (int i = 0; i < mUserPoints.size(); i++) {
                            mp = mUserPoints.get(i);
                            if (mp.getId().equals(mapPoint.getId())) {
                                if (valid && (i == 0 || mUserPoints.get(i - 1).getValidated() != MapPoint.NOT_VALIDATED))
                                    //es valido y el anterior no estaba fuera

                                    if (i != 0 && mp.getTime() - mUserPoints.get(i - 1).getTime() > 120000) {
                                        //es valido pero ha pasado mucho tiempo entre el anterior y este
                                        mp.setValidated(MapPoint.BIG_JUMP);
                                        Log.v("tagg", "bigjump1: con i: " + i + ", mappoints: " + mUserPoints.toString());
                                    } else {
                                        mp.setValidated(MapPoint.VALIDATED);
                                        mLights += mp.getLights();
                                    }


                                else if (valid && mUserPoints.get(i - 1).getValidated() == MapPoint.NOT_VALIDATED) {
                                    //es valido pero el anterior estaba fuera de camino
                                    mp.setValidated(MapPoint.BIG_JUMP);

                                    Log.v("tagg", "bigjump2: con i: " + i + ", mappoints: " + mUserPoints.toString());
                                } else
                                    //no es valido
                                    mp.setValidated(MapPoint.NOT_VALIDATED);
                            }
                        }
                        runTask();
                    }

                    @Override
                    public void OnError(String result, int resultCode, int resultType) {

                    }
                })).execute();
            }
        }

        switch (mActivityType) {
            case ActivityFragment.ACTIVITY_WALK:
                actionType = UpdateLights.WALK;
                break;
            case ActivityFragment.ACTIVITY_BIKE:
                actionType = UpdateLights.BIKE;
                break;

            case ActivityFragment.ACTIVITY_BUS:
                actionType = UpdateLights.BUS;
                break;

            case ActivityFragment.ACTIVITY_RAILROAD:
                actionType = UpdateLights.RAILROAD;
                break;

            case ActivityFragment.ACTIVITY_RECYCLE:
                actionType = UpdateLights.RECYCLE;
                break;

            case ActivityFragment.ACTIVITY_CARSHARE:
                actionType = UpdateLights.CAR_SHARE;
                break;

            default:
                actionType = UpdateLights.OTHER;
                break;

        }

        if (areAllValidated()) {
            Log.v("tagg", "end task all valid");
            new UpdateLights(mContext, (int) Math.round(Double.valueOf(mLights)), actionType, new OnTaskCompletedListener() {
                @Override
                public void OnComplete(String result, int resultCode, int resultType) {
                    Log.v("tagg", "end task lights updated");
                    if (resultCode == MyServerClass.NOT_CONNECTED) {
                        lightsSent = false;
                        if (mListener != null)
                            mListener.OnError(NOT_CONNECTED);
                    } else {
                        lightsSent = true;
                        if (mListener != null) {
                            mListener.OnUpdate(SUCCESSFUL);
                        } else {

                            Bitmap icon = BitmapFactory.decodeResource(mContext.getResources(),
                                    R.mipmap.ic_icono_app);

                            Notification notification = new NotificationCompat.Builder(mContext)
                                    .setContentTitle("¡Enhorabuena!")
                                    .setContentText("Se ha validado la actividad que realizaste sin conexión, has ganado " + mLights + " lights")
                                    .setTicker("Esto es un ticker")
                                    .setSmallIcon(R.drawable.ic_bulb)
                                    .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                                    .setOngoing(true)
                                    .build();

                            NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(mContext.NOTIFICATION_SERVICE);
                            notificationManager.notify(1, notification);
                        }

                    }
                }
            }).execute();
        } else {
            Log.v("tagg", "end task not all valid");
            if (mListener != null)
                mListener.OnError(NOT_VALIDATED);


        }
    }

    private boolean areAllValidated() {
        boolean res = true;
        for (MapPoint mp : mUserPoints) {
            if (mp.getValidated() == MapPoint.WAITING_VALIDATION) {
                res = false;
            }
        }

        return res;
    }

    public ArrayList<MapPoint> getUserPoints() {
        return mUserPoints;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EndActivityTask) {
            return ((EndActivityTask) obj).getUserPoints().equals(mUserPoints);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "user points: " + mUserPoints.toString();
    }
}
