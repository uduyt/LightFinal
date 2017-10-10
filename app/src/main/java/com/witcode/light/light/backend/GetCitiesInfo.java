package com.witcode.light.light.backend;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.facebook.Profile;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.witcode.light.light.domain.City;
import com.witcode.light.light.domain.MarketItem;
import com.witcode.light.light.domain.MarketItemAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class GetCitiesInfo extends MyServerClass implements OnTaskCompletedListener {

    private OnCityInfoCompleted mCallback;
    private ArrayList<City> mCities;
    private Context mContext;

    public GetCitiesInfo(Context context, OnCityInfoCompleted listener) {
        super(context);
        mCallback = listener;
        mContext=context;
        SetUp();
    }

    private void SetUp() {

        final String REQUEST_BASE_URL =
                "http://www.sustainabilight.com/functions/get_cities_info.php?";

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

            //Send exception to server

            Log.v("mytag", "listener_sent");
            //Send listener back
            mCallback.OnError(result, resultCode, resultType);

        } else {

            try {
                JSONArray jsonCities = new JSONArray(result);
                JSONObject jsonCity;
                mCities = new ArrayList<>();
                City city;
                for (int i = 0; i < jsonCities.length(); i++) {
                    jsonCity = jsonCities.getJSONObject(i);
                    city = new City();
                    city.setId(Integer.parseInt(jsonCity.getString("id")));
                    city.setName(jsonCity.getString("name"));
                    city.setBusEnabled(jsonCity.getInt("bus")==1);
                    city.setRailroadEnabled(jsonCity.getInt("railroad")==1);

                    PolygonOptions pOptions=new PolygonOptions();
                    pOptions.strokeColor(Color.parseColor("#01579B"));
                    pOptions.fillColor(Color.parseColor("#2203A9F4"));

                    JSONArray jsonLatLongs= new JSONArray(jsonCity.getString("polygon"));
                    for(int j=0;j<jsonLatLongs.length();j++){
                        pOptions.add(new LatLng(jsonLatLongs.getJSONObject(j).getDouble("lat"),jsonLatLongs.getJSONObject(j).getDouble("lng")));
                    }
                    city.setPolygonOptions(pOptions);

                    mCities.add(city);
                }

            } catch (JSONException e) {
                e.printStackTrace();
                mCallback.OnError(result,MyServerClass.ERROR,MyServerClass.WARNING);
            }

            mCallback.OnComplete(mCities);
        }

    }

}

