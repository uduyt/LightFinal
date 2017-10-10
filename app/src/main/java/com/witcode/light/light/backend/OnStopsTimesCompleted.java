package com.witcode.light.light.backend;

import android.os.Bundle;

import com.witcode.light.light.domain.MapPoint;

import java.util.ArrayList;

/**
 * Created by carlo on 09/03/2017.
 */

public interface OnStopsTimesCompleted {

    void OnComplete(ArrayList<Bundle> stopTimes);

    void OnError(String result, int resultCode, int resultType);
}
