package com.witcode.light.light.backend;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.witcode.light.light.Services.ActivityService;

/**
 * Created by rosety on 7/7/17.
 */

public class NetworkChangeReceiver extends BroadcastReceiver {
    private static final String TAG = "MyBroadcastReceiver";
    private OnConnectivityChangeListener mListener;

    public NetworkChangeReceiver(){

    }
    public NetworkChangeReceiver(OnConnectivityChangeListener listener) {
        mListener=listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        StringBuilder sb = new StringBuilder();
        sb.append("Action: " + intent.getAction() + "\n");
        sb.append("URI: " + intent.toUri(Intent.URI_INTENT_SCHEME).toString() + "\n");
        String log = sb.toString();
        Log.v(TAG, log);


        if(mListener!=null)
            mListener.OnChange(1);

    }
}