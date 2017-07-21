package com.witcode.light.light.backend;

import com.witcode.light.light.domain.MapPoint;

import java.util.ArrayList;

/**
 * Created by carlo on 09/03/2017.
 */

public interface OnPointsCompleteListener {

    void OnComplete(ArrayList<ArrayList<MapPoint>> points);

    void OnError(String result, int resultCode, int resultType);
}
