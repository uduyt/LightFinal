package com.witcode.light.light.backend;

import android.content.Context;
import android.net.Uri;

import com.facebook.Profile;

public class GetLights extends MyServerClass implements OnTaskCompletedListener {

    private OnTaskCompletedListener mCallback;
    private String type;

    public final static String TOTAL_LIGHTS="total_lights";
    public final static String SUMMED_LIGHTS="summed_lights";

    public GetLights(Context context, String type, OnTaskCompletedListener listener) {
        super(context);
        mCallback = listener;
        this.type=type;
        SetUp();
    }

    private void SetUp() {

        final String REQUEST_BASE_URL =
                "http://www.sustainabilight.com/functions/get_lights.php?";

        Uri builtUri = Uri.parse(REQUEST_BASE_URL).buildUpon()
                .appendQueryParameter("facebook_id", Profile.getCurrentProfile().getId())
                .appendQueryParameter("type", type)
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


