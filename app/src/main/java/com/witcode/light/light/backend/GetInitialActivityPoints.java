package com.witcode.light.light.backend;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.facebook.Profile;
import com.witcode.light.light.domain.MapPoint;

import java.util.ArrayList;

public class GetInitialActivityPoints extends MyServerClass implements OnTaskCompletedListener {

    private OnInitialPointsCompleteListener mCallback;
    private ArrayList<MapPoint> mPoints;

    public GetInitialActivityPoints(Context context, OnInitialPointsCompleteListener listener) {
        super(context);
        mCallback = listener;
        mPoints=new ArrayList<>();
        SetUp();
    }

    private void SetUp() {

        final String REQUEST_BASE_URL =
                "http://www.sustainabilight.com/functions/get_initial_activity_points.php?";

        Uri builtUri = Uri.parse(REQUEST_BASE_URL).buildUpon()
                .appendQueryParameter("facebook_id", Profile.getCurrentProfile().getId())
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
            mCallback.OnComplete(mPoints);

        } else {
            mCallback.OnError(result,resultCode,resultType);
        }

    }

}

