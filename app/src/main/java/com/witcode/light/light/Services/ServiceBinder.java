package com.witcode.light.light.Services;

import android.location.Location;

import com.google.android.gms.maps.model.PolylineOptions;
import com.witcode.light.light.domain.MapPoint;

import java.util.ArrayList;

/**
 * Created by carlo on 09/03/2017.
 */

public interface ServiceBinder {

    void OnNewMapPoint(double distance, double speed, PolylineOptions polylineOptions);
    void OnGPSUpdate(String GPS);
    void OnPolylineUpdate(int index, int color);
    void OnInitialPointsArrived(ArrayList<PolylineOptions> polylineOptions);
}
