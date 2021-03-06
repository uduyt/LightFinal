package com.witcode.light.light.backend;

import android.content.Context;
import android.net.Uri;

import com.facebook.Profile;

public class GetRankings extends MyServerClass implements OnTaskCompletedListener {

    private OnTaskCompletedListener mCallback;

    public GetRankings(Context context, OnTaskCompletedListener listener) {
        super(context);
        mCallback = listener;

        SetUp();
    }

    private void SetUp() {

        final String REQUEST_BASE_URL =
                "http://www.sustainabilight.com/functions/get_rankings.php?";

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
            mCallback.OnComplete(result, resultCode, resultType);

        } else {
            mCallback.OnComplete(result, SUCCESSFUL, SUCCESSFUL);
        }

    }

}



