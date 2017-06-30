package com.witcode.light.light.backend;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.facebook.Profile;

public class ExceptionHandler extends MyServerClass implements OnTaskCompletedListener {

    private String mResult;
    private int mResultCode, mResultType;
    public static final int SUCCESSFUL=1;

    public ExceptionHandler(Context context, String result) {
        super(context,false);
        mResult=result;

        SetUp();
    }

    private void SetUp(){

        final String REQUEST_BASE_URL =
                "http://www.sustainabilight.com/functions/handle_exception.php?";

        Uri builtUri = Uri.parse(REQUEST_BASE_URL).buildUpon()
                .appendQueryParameter("facebook_id", Profile.getCurrentProfile().getId())
                .appendQueryParameter("exception", mResult)
                .build();


        super.setUri(builtUri);
        super.setListener(this);

        Log.v("mytag", "exception sent with the following message: " + mResult);

    }

    @Override
    public void OnComplete(String result, int resultCode, int resultType) {
        if(resultType==SUCCESSFUL){
            Log.v("mytag", "exception was correctly received: " + mResult);
        }

    }

}


