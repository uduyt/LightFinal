package com.witcode.light.light.backend;

import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.support.v7.widget.DefaultItemAnimator;
import android.util.Log;

import com.facebook.Profile;
import com.google.android.gms.maps.model.LatLng;
import com.witcode.light.light.Utils;
import com.witcode.light.light.domain.EastNorth;
import com.witcode.light.light.domain.MapPoint;
import com.witcode.light.light.domain.Notification;
import com.witcode.light.light.domain.NotificationsAdapter;
import com.witcode.light.light.fragments.ActivityFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

public class GetNearRoutePoints extends MyServerClass implements OnTaskCompletedListener {

    private OnPointsCompleteListener mCallback;
    private String Line;
    private int activityType;
    private ArrayList<MapPoint> mPoints;
    private ArrayList<ArrayList<MapPoint>> mFinalPoints;
    private boolean urban_cercanias; //urban or metro

    public GetNearRoutePoints(Context context, boolean urban, int activityType, String line, OnPointsCompleteListener listener) {
        super(context);
        mCallback = listener;
        Line = line;
        this.activityType = activityType;
        this.urban_cercanias=urban;
        SetUp();
    }

    private void SetUp() {

        Uri builtUri;
        final String REQUEST_BASE_URL;
        EastNorth eastNorth;
        Double Easting, Northing;
        switch (activityType) {
            case ActivityFragment.ACTIVITY_BUS:

                if(ActivityFragment.currentCity.getId()==1) {
                    //Madrid

                    if (urban_cercanias) {
                        REQUEST_BASE_URL =
                                "http://www.sustainabilight.com/functions/get_near_urban_bus_route.php?";


                        builtUri = Uri.parse(REQUEST_BASE_URL).buildUpon()
                                .appendQueryParameter("line", Line)
                                .build();
                    } else {
                        REQUEST_BASE_URL =
                                "http://www.sustainabilight.com/functions/get_near_interurban_bus_route.php?";


                        builtUri = Uri.parse(REQUEST_BASE_URL).buildUpon()
                                .appendQueryParameter("line", Line)
                                .build();
                    }
                }else{
                    //Gran Canaria
                    REQUEST_BASE_URL =
                            "http://www.sustainabilight.com/functions/get_near_bus_route_canarias.php?";


                    builtUri = Uri.parse(REQUEST_BASE_URL).buildUpon()
                            .appendQueryParameter("line", Line)
                            .build();
                }
                break;

            case ActivityFragment.ACTIVITY_RAILROAD:
                if (!urban_cercanias) { //metro
                    REQUEST_BASE_URL =
                            "http://www.sustainabilight.com/functions/get_near_metro_route.php?";


                    builtUri = Uri.parse(REQUEST_BASE_URL).buildUpon()
                            .appendQueryParameter("line", Line)
                            .build();


                }else{ //cercanias
                    REQUEST_BASE_URL =
                            "http://www.sustainabilight.com/functions/get_near_cercanias_route.php?";


                    builtUri = Uri.parse(REQUEST_BASE_URL).buildUpon()
                            .appendQueryParameter("line", Line)
                            .build();
                }

                break;
            default:
                REQUEST_BASE_URL =
                        "http://www.sustainabilight.com/functions/get_near_bus_route.php?";

                builtUri = Uri.parse(REQUEST_BASE_URL).buildUpon()
                        .appendQueryParameter("line", Line)
                        .build();
                break;
        }


        super.setUri(builtUri);
        super.setListener(this);

        Log.v("taggg","url: " + builtUri.toString());

    }

    @Override
    public void OnComplete(String result, int resultCode, int resultType) {
        Log.v("taggg","onComplete: result: " + result + ", resultCode: " + resultCode + ", resultType: " + resultType);

        if (resultType != MyServerClass.SUCCESSFUL) {
            if (resultCode == MyServerClass.NULL_RESULT) {
                //maybe it doesnt matter
            }


            mCallback.OnError(result, SUCCESSFUL, SUCCESSFUL);

        } else {


            Log.v("taggg",result);
            try {
                JSONArray jsonPoints = new JSONArray(result);
                JSONArray jsonPoints2;
                JSONObject jsonPoint;

                mFinalPoints = new ArrayList<>();
                MapPoint mapPoint;
                for (int i = 0; i < jsonPoints.length(); i++) {
                    jsonPoints2 = jsonPoints.getJSONArray(i);

                    mPoints = new ArrayList<>();
                    for(int j=0;j<jsonPoints2.length();j++){
                        mapPoint = new MapPoint();
                        jsonPoint=jsonPoints2.getJSONObject(j);
                        mapPoint.setLatLng(new LatLng(Double.parseDouble(jsonPoint.getString("lat")),Double.parseDouble(jsonPoint.getString("long"))));
                        mapPoint.setType(MapPoint.TRANSPORT_ROUTE);
                        mPoints.add(mapPoint);
                    }
                    mFinalPoints.add(mPoints);

                }

            } catch (JSONException e) {
                e.printStackTrace();
                mCallback.OnError("no_line",resultCode,resultType);
            }
            mCallback.OnComplete(mFinalPoints);
        }

    }

}

