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

public class UpdateUserData extends MyServerClass implements OnTaskCompletedListener {

    private OnTaskCompletedListener mCallback;
    private Context mContext;
    LocationManager mLocationManager;
    Location myLocation;

    public UpdateUserData(Context context, OnTaskCompletedListener listener) {
        super(context);
        mCallback = listener;
        mContext=context;
        myLocation=getLastKnownLocation();
        SetUp();
    }

    private void SetUp() {

        final String REQUEST_BASE_URL =
                "http://www.sustainabilight.com/functions/update_user_data.php?";

        String version;
        try {
            PackageInfo pInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            version = String.valueOf(pInfo.versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            version="-1";
        }

        String lat,longg;
        if(myLocation==null || myLocation.getAccuracy()>200){
            lat="null";
            longg="null";
        }else{
            lat=String.valueOf(myLocation.getLatitude());
            longg=String.valueOf(myLocation.getLongitude());
        }

        Uri builtUri = Uri.parse(REQUEST_BASE_URL).buildUpon()
                .appendQueryParameter("facebook_id", Profile.getCurrentProfile().getId())
                .appendQueryParameter("app_version", version)
                .appendQueryParameter("lat",lat)
                .appendQueryParameter("long",longg)
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

    private Location getLastKnownLocation() {
        mLocationManager = (LocationManager) mContext.getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            Location l;
            try{
                l= mLocationManager.getLastKnownLocation(provider);
            }catch (SecurityException e){
                l=null;
            }

            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }

}


