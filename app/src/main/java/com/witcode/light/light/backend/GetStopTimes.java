package com.witcode.light.light.backend;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.facebook.Profile;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.witcode.light.light.domain.MapPoint;
import com.witcode.light.light.fragments.ActivityFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class GetStopTimes extends MyServerClass implements OnTaskCompletedListener {

    private OnStopsTimesCompleted mCallback;
    private ArrayList<Bundle> mStopTimes;
    private Context mContext;
    private String mStopId;

    public GetStopTimes(Context context,String stopId, OnStopsTimesCompleted listener) {
        super(context);
        mCallback = listener;
        mContext=context;
        mStopId=stopId;
        SetUp();
    }

    private void SetUp() {

        final String REQUEST_BASE_URL =
                "http://www.sustainabilight.com/functions/get_stop_times.php?";

        Uri builtUri = Uri.parse(REQUEST_BASE_URL).buildUpon()
                .appendQueryParameter("facebook_id", Profile.getCurrentProfile().getId())
                .appendQueryParameter("stop_id",mStopId)
                .build();


        super.setUri(builtUri);
        super.setListener(this);

    }

    @Override
    public void OnComplete(String result, int resultCode, int resultType) {
        if (resultType != MyServerClass.SUCCESSFUL) {
            if (resultCode == MyServerClass.NULL_RESULT) {
                //maybe it doesnt matter
            }

            //Send exception to server

            Log.v("mytag", "listener_sent");
            //Send listener back
            mCallback.OnError(result, resultCode, resultType);

        } else {

            try {
                JSONArray jsonStopTimes = new JSONArray(result);
                JSONObject jsonStopTime;
                mStopTimes = new ArrayList<>();
                Bundle bundle;
                for (int i = 0; i < jsonStopTimes.length(); i++) {
                    jsonStopTime = jsonStopTimes.getJSONObject(i);
                    bundle = new Bundle();
                    bundle.putString("line",jsonStopTime.getString("line"));
                    bundle.putString("destination",jsonStopTime.getString("destination"));
                    bundle.putString("timelapse",jsonStopTime.getString("timelapse"));

                    mStopTimes.add(bundle);
                }

            } catch (JSONException e) {
                e.printStackTrace();
                mCallback.OnError(result,MyServerClass.ERROR,MyServerClass.WARNING);
            }

            mCallback.OnComplete(mStopTimes);
        }
    }
}