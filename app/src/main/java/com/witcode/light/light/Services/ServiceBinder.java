package com.witcode.light.light.Services;

import android.location.Location;

import com.witcode.light.light.domain.MapPoint;

import java.util.ArrayList;

/**
 * Created by carlo on 09/03/2017.
 */

public interface ServiceBinder {

    void OnUpdate(double distance, double speed, int seconds, double lights, ArrayList<MapPoint> points);
    void OnGPSUpdate(String GPS);
}
