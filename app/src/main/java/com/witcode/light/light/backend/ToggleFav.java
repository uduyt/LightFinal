package com.witcode.light.light.backend;

import android.content.Context;
import android.net.Uri;

import com.facebook.Profile;

public class ToggleFav extends MyServerClass implements OnTaskCompletedListener {

    private OnTaskCompletedListener mCallback;
    private String mNotifId, mFav;

    public ToggleFav(Context context, String fav, String notifId, OnTaskCompletedListener listener) {
        super(context);
        mCallback = listener;
        mNotifId=notifId;
        mFav=fav;
        SetUp();
    }

    private void SetUp() {

        final String REQUEST_BASE_URL =
                "http://www.sustainabilight.com/functions/toggle_fav.php?";

        Uri builtUri = Uri.parse(REQUEST_BASE_URL).buildUpon()
                .appendQueryParameter("facebook_id", Profile.getCurrentProfile().getId())
                .appendQueryParameter("notif_id", mNotifId)
                .appendQueryParameter("fav", mFav)
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


            //Send listener back
            mCallback.OnComplete(result, resultCode, resultType);

        } else {
            mCallback.OnComplete(result, SUCCESSFUL, SUCCESSFUL);
        }

    }

}


