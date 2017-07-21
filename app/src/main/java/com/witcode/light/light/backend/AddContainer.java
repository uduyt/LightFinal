package com.witcode.light.light.backend;

import android.content.Context;
import android.location.Location;
import android.net.Uri;

import com.facebook.Profile;

public class AddContainer extends MyServerClass implements OnTaskCompletedListener {

    private OnTaskCompletedListener mCallback;
    private Location mLocation;

    public AddContainer(Context context, Location location, OnTaskCompletedListener listener) {
        super(context);
        mCallback = listener;
        mLocation=location;
        SetUp();
    }

    private void SetUp() {

        final String REQUEST_BASE_URL =
                "http://www.sustainabilight.com/functions/add_container.php?";

        Uri builtUri = Uri.parse(REQUEST_BASE_URL).buildUpon()
                .appendQueryParameter("accuracy", String.valueOf(mLocation.getAccuracy()))
                .appendQueryParameter("user_created", Profile.getCurrentProfile().getId())
                .appendQueryParameter("lat", String.valueOf(mLocation.getLatitude()))
                .appendQueryParameter("long", String.valueOf(mLocation.getLongitude()))
                .build();


        super.setUri(builtUri);
        super.setListener(this);

    }

    @Override
    public void OnComplete(String result, int resultCode, int resultType) {

            mCallback.OnComplete(result, resultCode, resultType);

    }

}


