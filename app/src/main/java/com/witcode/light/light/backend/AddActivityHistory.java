package com.witcode.light.light.backend;

import android.content.Context;
import android.net.Uri;

import com.facebook.Profile;

public class AddActivityHistory extends MyServerClass implements OnTaskCompletedListener {

    private OnTaskCompletedListener mCallback;
    private String lights, speed, time, distance;
    private String actionType;

    public final static String WALK="walk";
    public final static String BIKE="bike";
    public final static String RAILROAD="railroad";
    public final static String BUS="bus";
    public final static String RECYCLE="recycle";
    public final static String CAR_SHARE="car_share";
    public final static String OTHER="other";

    public AddActivityHistory(Context context, String l, String speed, String time, String distance, String actionType, OnTaskCompletedListener listener) {
        super(context);
        mCallback=listener;
        lights=l;
        this.actionType=actionType;
        this.speed=speed;
        this.time=time;
        this.distance=distance;

        SetUp();
    }

    private void SetUp(){

        final String REQUEST_BASE_URL =
                "http://www.sustainabilight.com/functions/add_activity_history.php?";

        Uri builtUri = Uri.parse(REQUEST_BASE_URL).buildUpon()
                .appendQueryParameter("facebook_id", Profile.getCurrentProfile().getId())
                .appendQueryParameter("lights",String.valueOf(lights))
                .appendQueryParameter("speed",String.valueOf(speed))
                .appendQueryParameter("time",String.valueOf(time))
                .appendQueryParameter("distance",String.valueOf(distance))
                .appendQueryParameter("action_type",actionType)
                .build();


        super.setUri(builtUri);
        super.setListener(this);

    }

    @Override
    public void OnComplete(String result, int resultCode, int resultType) {
        if(resultType!=MyServerClass.SUCCESSFUL){
            if(resultCode==MyServerClass.NULL_RESULT){
                //maybe it doesnt matter
            }

            //Send exception to server


            //Send listener back
            mCallback.OnComplete(result,resultCode,resultType);

        }else{
            if (result.equals("ok")) {
                mCallback.OnComplete(result,SUCCESSFUL, SUCCESSFUL);
            }
        }

    }

}




