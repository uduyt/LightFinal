package com.witcode.light.light.backend;

import com.witcode.light.light.domain.MapPoint;

import java.util.Map;

/**
 * Created by carlo on 09/03/2017.
 */

public interface CheckIfValidListener {

    void OnComplete(boolean valid, MapPoint mapPoint);
    void OnError(String result, int resultCode, int resultType);
}
