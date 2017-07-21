package com.witcode.light.light.backend;

import android.content.Context;
import android.net.Uri;

import com.facebook.Profile;
import com.witcode.light.light.Utils;

public class CheckIfLineExists extends MyServerClass implements OnTaskCompletedListener {

    private CheckIfLineExistsListener mCallback;
    private String mLine;
    private boolean Urban=true;

    public CheckIfLineExists(Context context, String line, CheckIfLineExistsListener listener) {
        super(context);
        mCallback = listener;
        mLine = line;
        SetUp();
    }

    private void SetUp() {

        final String REQUEST_BASE_URL =
                "http://www.sustainabilight.com/functions/check_if_line_exists.php?";


        Uri builtUri = Uri.parse(REQUEST_BASE_URL).buildUpon()
                .appendQueryParameter("line", mLine)
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


            mCallback.OnError(result,resultCode,resultType);

        } else {
            //Send listener back

            switch (result){
                case "urban":
                    mCallback.onComplete(true,true);
                    break;
                case "interurban":
                    mCallback.onComplete(true,false);
                    break;
                case "none":
                    mCallback.onComplete(false,false);
                    break;
                default:
                    mCallback.OnError(result,resultCode,resultType);
                    break;
            }

        }

    }

}


