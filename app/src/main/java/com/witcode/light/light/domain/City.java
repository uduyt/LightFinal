package com.witcode.light.light.domain;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;

/**
 * Created by rosety on 29/9/17.
 */

public class City {
    private int id;
    private String name;
    private PolygonOptions polygonOptions;
    private boolean busEnabled, railroadEnabled;

    public City(){

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PolygonOptions getPolygonOptions() {
        return polygonOptions;
    }

    public void setPolygonOptions(PolygonOptions polygonOptions) {
        this.polygonOptions = polygonOptions;
    }

    public boolean isBusEnabled() {
        return busEnabled;
    }

    public void setBusEnabled(boolean busEnabled) {
        this.busEnabled = busEnabled;
    }

    public boolean isRailroadEnabled() {
        return railroadEnabled;
    }

    public void setRailroadEnabled(boolean railroadEnabled) {
        this.railroadEnabled = railroadEnabled;
    }
}
