package com.witcode.light.light.backend;

/**
 * Created by carlo on 09/03/2017.
 */

public interface CheckIfLineExistsListener {

    void onComplete(boolean exists, boolean urban);

    void OnError(String result, int resultCode, int resultType);
}
