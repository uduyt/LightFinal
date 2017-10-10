package com.witcode.light.light.backend;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;

import com.facebook.Profile;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolygonOptions;
import com.witcode.light.light.domain.City;
import com.witcode.light.light.domain.MapPoint;
import com.witcode.light.light.fragments.ActivityFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class GetInitialStopMapPoints extends MyServerClass implements OnTaskCompletedListener {

    private OnStopsCompleted mCallback;
    private ArrayList<MapPoint> mMapPoints;
    private LatLngBounds mBounds;
    private Context mContext;

    public GetInitialStopMapPoints(Context context, LatLngBounds bounds, OnStopsCompleted listener) {
        super(context);
        mCallback = listener;
        mContext=context;
        mBounds=bounds;
        SetUp();
    }

    private void SetUp() {

        final String REQUEST_BASE_URL =
                "http://www.sustainabilight.com/functions/get_initial_stops.php?";

        Uri builtUri = Uri.parse(REQUEST_BASE_URL).buildUpon()
                .appendQueryParameter("facebook_id", Profile.getCurrentProfile().getId())
                .appendQueryParameter("northeast_lat", String.valueOf(mBounds.northeast.latitude))
                .appendQueryParameter("northeast_long", String.valueOf(mBounds.northeast.longitude))
                .appendQueryParameter("southwest_lat", String.valueOf(mBounds.southwest.latitude))
                .appendQueryParameter("southwest_long", String.valueOf(mBounds.southwest.longitude))
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
                JSONArray jsonMapPoints = new JSONArray(result);
                JSONObject jsonMapPoint;
                mMapPoints = new ArrayList<>();
                MapPoint mapPoint;
                for (int i = 0; i < jsonMapPoints.length(); i++) {
                    jsonMapPoint = jsonMapPoints.getJSONObject(i);
                    mapPoint = new MapPoint();
                    mapPoint.setId(jsonMapPoint.getString("stop_id"));
                    mapPoint.setLatLng(new LatLng(Double.valueOf(jsonMapPoint.getString("lat")),Double.valueOf(jsonMapPoint.getString("longg"))));
                    mapPoint.setType(ActivityFragment.ACTIVITY_BUS);
                    mapPoint.setName(jsonMapPoint.getString("name"));

                    mMapPoints.add(mapPoint);
                }

            } catch (JSONException e) {
                e.printStackTrace();
                mCallback.OnError(result,MyServerClass.ERROR,MyServerClass.WARNING);
            }

            mCallback.OnComplete(mMapPoints);
        }

    }

}

