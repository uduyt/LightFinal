package backend;

import android.location.Location;
import android.support.annotation.Nullable;

/**
 * Created by carlo on 09/03/2017.
 */

public interface OnLocationUpdateListener {

    void OnLocationLoad(Location location);
    void OnLocationTimeOut(@Nullable Location location);
}
