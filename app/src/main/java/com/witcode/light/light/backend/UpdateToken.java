package com.witcode.light.light.backend;

import android.content.Context;
import android.net.Uri;

import com.facebook.Profile;

public class UpdateToken extends MyServerClass implements OnTaskCompletedListener {

    private OnTaskCompletedListener mCallback;
    private String mToken;
    private String Id;

    public UpdateToken(Context context, String token, OnTaskCompletedListener listener) {
        super(context);
        mCallback=listener;
        mToken=token;

        if(Profile.getCurrentProfile()!=null && Profile.getCurrentProfile().getId()!=null){
            Id=Profile.getCurrentProfile().getId();
            SetUp();
        }

    }

    private void SetUp(){

        final String REQUEST_BASE_URL =
                "http://www.sustainabilight.com/functions/update_token.php?";

        Uri builtUri = Uri.parse(REQUEST_BASE_URL).buildUpon()
                .appendQueryParameter("facebook_id", Id)
                .appendQueryParameter("token",mToken)

                .build();

        super.setUri(builtUri);
        super.setListener(this);

    }

    @Override
    public void OnComplete(String result, int resultCode, int resultType) {
        if(resultType!=MyServerClass.SUCCESSFUL){
            if(resultCode==MyServerClass.NULL_RESULT){
                //maybe it doesnt matter
            }

            //Send exception to server


            //Send listener back
            mCallback.OnComplete(result,resultCode,resultType);

        }else{
            if (result.equals("ok")) {
                mCallback.OnComplete(result,SUCCESSFUL, SUCCESSFUL);
            }
        }

    }

}




