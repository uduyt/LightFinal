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

public class ValidateInterurbanBus extends MyServerClass implements OnTaskCompletedListener {


    private OnTaskCompletedListener mCallback;
    private HttpURLConnection urlConnection;
    private String TAG = "tagg";
    public static final int VALIDATED = 1;
    public static final int TOO_FAR = 2;
    private Location mLocation;
    private String mLine;



    public
    ValidateInterurbanBus(Context context, String line, Location location, OnTaskCompletedListener listener) {
        super(context);
        mCallback = listener;
        mLocation = location;
        mLine = line;

        SetUp();
    }

    private void SetUp() {

        double Easting;
        double Northing;

        final String REQUEST_BASE_URL =
                "http://www.sustainabilight.com/functions/validate_interurban_bus.php?";

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

            String line, lines;
            int index;

            for (int i = 0; i < features_json.length(); i++) {
                lines = features_json.getJSONObject(i).getJSONObject("attributes").getString("LINEAS");
                while (lines.contains(",")) {
                    index = lines.indexOf(",");
                    line = lines.substring(0, index);
                    lines = lines.substring(index + 2);
                    if (mLine.equals(line)) {
                        args.setResultCode(VALIDATED);
                        return args;
                    }
                }
                if (mLine.equals(lines)) {
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


