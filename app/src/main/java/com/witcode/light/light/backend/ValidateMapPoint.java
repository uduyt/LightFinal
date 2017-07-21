package com.witcode.light.light.backend;

import android.content.Context;
import android.location.Location;
import android.net.Uri;

import com.witcode.light.light.Utils;
import com.witcode.light.light.domain.EastNorth;
import com.witcode.light.light.domain.MapPoint;
import com.witcode.light.light.fragments.ActivityFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;

public class ValidateMapPoint extends MyServerClass implements OnTaskCompletedListener {


    private CheckIfValidListener mCallback;
    private HttpURLConnection urlConnection;
    private String TAG = "tagg";
    public static final int VALIDATED = 1;
    public static final int TOO_FAR = 2;
    private MapPoint mapPoint;
    private String mLine;
    private int activityType;
    private boolean urban = true; //cercanias

    public ValidateMapPoint(Context context, String line, int activityType, boolean urban, MapPoint mapPoint, CheckIfValidListener listener) {
        super(context);
        mCallback = listener;
        this.mapPoint = mapPoint;
        mLine = line;
        this.activityType = activityType;
        this.urban = urban;
        SetUp();
    }

    private void SetUp() {

        Uri builtUri;
        final String REQUEST_BASE_URL;
        EastNorth eastNorth;
        Double Easting, Northing;
        switch (activityType) {
            case ActivityFragment.ACTIVITY_BUS:

                if (urban) {
                    REQUEST_BASE_URL =
                            "http://www.sustainabilight.com/functions/validate_urban_bus_map_point.php?";

                    eastNorth = Utils.Deg2UTM(mapPoint.getLatLng().latitude,mapPoint.getLatLng().longitude);

                    Easting = eastNorth.getEasting();
                    Northing = eastNorth.getNorthing();

                    builtUri = Uri.parse(REQUEST_BASE_URL).buildUpon()
                            .appendQueryParameter("line", mLine)
                            .appendQueryParameter("easting", String.valueOf(Easting))
                            .appendQueryParameter("northing", String.valueOf(Northing))
                            .build();
                } else {
                    REQUEST_BASE_URL =
                            "http://www.sustainabilight.com/functions/validate_interurban_bus_map_point.php?";

                    eastNorth = Utils.Deg2UTM(mapPoint.getLatLng().latitude,mapPoint.getLatLng().longitude);

                    Easting = eastNorth.getEasting();
                    Northing = eastNorth.getNorthing();

                    builtUri = Uri.parse(REQUEST_BASE_URL).buildUpon()
                            .appendQueryParameter("line", mLine)
                            .appendQueryParameter("easting", String.valueOf(Easting))
                            .appendQueryParameter("northing", String.valueOf(Northing))
                            .build();
                }

                break;

            case ActivityFragment.ACTIVITY_RAILROAD:

                if(!urban){ //metro
                    REQUEST_BASE_URL =
                            "http://www.sustainabilight.com/functions/validate_metro_map_point.php?";

                    eastNorth = Utils.Deg2UTM(mapPoint.getLatLng().latitude,mapPoint.getLatLng().longitude);

                    Easting = eastNorth.getEasting();
                    Northing = eastNorth.getNorthing();

                    builtUri = Uri.parse(REQUEST_BASE_URL).buildUpon()
                            .appendQueryParameter("line", mLine)
                            .appendQueryParameter("easting", String.valueOf(Easting))
                            .appendQueryParameter("northing", String.valueOf(Northing))
                            .build();
                }else{
                    REQUEST_BASE_URL =
                            "http://www.sustainabilight.com/functions/validate_cercanias_map_point.php?";

                    eastNorth = Utils.Deg2UTM(mapPoint.getLatLng().latitude,mapPoint.getLatLng().longitude);

                    Easting = eastNorth.getEasting();
                    Northing = eastNorth.getNorthing();

                    builtUri = Uri.parse(REQUEST_BASE_URL).buildUpon()
                            .appendQueryParameter("line", mLine)
                            .appendQueryParameter("easting", String.valueOf(Easting))
                            .appendQueryParameter("northing", String.valueOf(Northing))
                            .build();
                }

                break;

            default:
                REQUEST_BASE_URL =
                        "http://www.sustainabilight.com/functions/validate_interurban_bus_map_point.php?";

                eastNorth = Utils.Deg2UTM(mapPoint.getLatLng().latitude,mapPoint.getLatLng().longitude);

                Easting = eastNorth.getEasting();
                Northing = eastNorth.getNorthing();

                builtUri = Uri.parse(REQUEST_BASE_URL).buildUpon()
                        .appendQueryParameter("line", mLine)
                        .appendQueryParameter("easting", String.valueOf(Easting))
                        .appendQueryParameter("northing", String.valueOf(Northing))
                        .build();
                break;
        }

        super.setUri(builtUri);
        super.setListener(this);

    }

    @Override
    public void OnComplete(String result, int resultCode, int resultType) {
        if (resultType != MyServerClass.SUCCESSFUL) {
            if (resultCode == MyServerClass.NULL_RESULT) {
                //maybe it doesnt matter
            }
            //Send listener back
            mCallback.OnError(result, resultCode, resultType);

        } else {

            if(result.equals("valid"))
                mCallback.OnComplete(true,mapPoint);
            else if(result.equals("not_valid"))
                mCallback.OnComplete(false,mapPoint);
            else
                mCallback.OnError(result, resultCode, resultType);
        }

    }


}


