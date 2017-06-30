package com.witcode.light.light.Services;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.witcode.light.light.backend.OnTaskCompletedListener;
import com.witcode.light.light.backend.UpdateToken;

/**
 * Created by rosety on 2/6/17.
 */

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private Context mContext= this;
    @Override
    public void onTokenRefresh() {
        final String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d("mytag", "Refreshed token: " + refreshedToken);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.

        Handler mainHandler = new Handler(getMainLooper());

        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                new UpdateToken(mContext, refreshedToken, new OnTaskCompletedListener() {
                    @Override
                    public void OnComplete(String result, int resultCode, int resultType) {

                    }
                }).execute();
            } // This is your code
        };
        mainHandler.post(myRunnable);



    }
}
