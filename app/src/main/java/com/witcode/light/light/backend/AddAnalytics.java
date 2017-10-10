package com.witcode.light.light.backend;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;

import com.facebook.Profile;

import java.util.List;

import static android.content.Context.LOCATION_SERVICE;

public class AddAnalytics extends MyServerClass implements OnTaskCompletedListener {

    private OnTaskCompletedListener mCallback;
    private Context mContext;
    private String type, val1, val2, val3;

    public AddAnalytics(Context context, String type, String val1, String val2, String val3, OnTaskCompletedListener listener) {
        super(context);
        mCallback = listener;
        mContext=context;
        this.type=type;
        this.val1=val1;
        this.val2=val2;
        this.val3=val3;

        SetUp();
    }

    private void SetUp() {

        final String REQUEST_BASE_URL =
                "http://www.sustainabilight.com/functions/add_analytics.php?";

        String version;
        try {
            PackageInfo pInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            version = String.valueOf(pInfo.versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            version="-1";
        }

        Uri builtUri = Uri.parse(REQUEST_BASE_URL).buildUpon()
                .appendQueryParameter("facebook_id", Profile.getCurrentProfile().getId())
                .appendQueryParameter("app_version", version)
                .appendQueryParameter("type",type)
                .appendQueryParameter("val1",val1)
                .appendQueryParameter("val2",val2)
                .appendQueryParameter("val3",val3)
                .build();


        super.setUri(builtUri);
        super.setListener(this);

    }

    @Override
    public void OnComplete(String result, int resultCode, int resultType) {
        if (resultType != MyServerClass.SUCCESSFUL) {

            //Send listener back
            mCallback.OnComplete(result, resultCode, resultType);

        } else {
            mCallback.OnComplete(result, SUCCESSFUL, SUCCESSFUL);
        }

    }

}


