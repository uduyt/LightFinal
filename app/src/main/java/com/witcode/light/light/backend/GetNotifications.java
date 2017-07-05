package com.witcode.light.light.backend;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.facebook.Profile;

public class GetNotifications extends MyServerClass implements OnTaskCompletedListener {

    private OnTaskCompletedListener mCallback;
    private int mLimit;

    public GetNotifications(Context context, int limit, OnTaskCompletedListener listener) {
        super(context);
        mCallback = listener;
        mLimit=limit;
        SetUp();
    }

    private void SetUp() {

        final String REQUEST_BASE_URL =
                "http://www.sustainabilight.com/functions/get_notifications.php?";

        Uri builtUri = Uri.parse(REQUEST_BASE_URL).buildUpon()
                .appendQueryParameter("facebook_id", Profile.getCurrentProfile().getId())
                .appendQueryParameter("limit", String.valueOf(mLimit))
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
            mCallback.OnComplete(result, resultCode, resultType);

        } else {
            mCallback.OnComplete(result, SUCCESSFUL, SUCCESSFUL);
        }

    }

}

