package com.witcode.light.light.backend;

import android.content.Context;
import android.location.Location;
import android.net.Uri;

import com.witcode.light.light.Utils;
import com.witcode.light.light.domain.EastNorth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;

public class ValidateCercanias extends MyServerClass implements OnTaskCompletedListener {


    private OnTaskCompletedListener mCallback;
    private HttpURLConnection urlConnection;
    private String TAG="tagg";
    public static final int VALIDATED = 1;
    public static final int TOO_FAR = 2;
    private Location mLocation;
    private String mStation;

    public ValidateCercanias(Context context, String station, Location location, OnTaskCompletedListener listener) {
        super(context);
        mCallback = listener;
        mLocation = location;
        mStation = station;

        SetUp();
    }

    private void SetUp(){

        double Easting;
        double Northing;

        final String REQUEST_BASE_URL =
                "http://www.sustainabilight.com/functions/validate_cercanias.php?";

        EastNorth eastNorth= Utils.Deg2UTM(mLocation.getLatitude(), mLocation.getLongitude());

        Easting=eastNorth.getEasting();
        Northing=eastNorth.getNorthing();


        Uri builtUri = Uri.parse(REQUEST_BASE_URL).buildUpon()
                .appendQueryParameter("easting", String.valueOf(Easting))
                .appendQueryParameter("northing", String.valueOf(Northing))
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
            //Send listener back
            mCallback.OnComplete(result, resultCode, resultType);

        } else {
            ListenerArguments args = HandleResult(new ListenerArguments(result, resultCode, resultType));
            mCallback.OnComplete(args.getResult(), args.getResultCode(), args.getResultType());
        }

    }

    private ListenerArguments HandleResult(ListenerArguments args) {
        try {
            JSONObject res_json = new JSONObject(args.getResult());

            JSONArray features_json = res_json.getJSONArray("features");
            String station;

            for (int i = 0; i < features_json.length(); i++) {
                station = features_json.getJSONObject(i).getJSONObject("attributes").getString("DENOMINACION");

                if (mStation.toUpperCase().equals(station)) {
                    args.setResultCode(VALIDATED);
                    return args;
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
            args.setResultCode(TOO_FAR);
            return args;
        }
        args.setResultCode(TOO_FAR);
        return args;
    }


}


