package com.witcode.light.light.backend;

import android.content.Context;
import android.location.Location;
import android.net.Uri;

public class GetDistanceToNearestContainer extends MyServerClass{

    private OnTaskCompletedListener mCallback;
    private Location mLocation;

    public GetDistanceToNearestContainer(Context context, Location location, OnTaskCompletedListener listener) {
        super(context);
        mCallback = listener;
        mLocation = location;
        SetUp();
    }

    private void SetUp() {

        final String REQUEST_BASE_URL =
                "http://www.sustainabilight.com/functions/get_distance_to_container.php?";

        Uri builtUri = Uri.parse(REQUEST_BASE_URL).buildUpon()
                .appendQueryParameter("lat", String.valueOf(mLocation.getLatitude()))
                .appendQueryParameter("long", String.valueOf(mLocation.getLongitude()))
                .build();

        super.setUri(builtUri);
        super.setListener(mCallback);

    }

}


