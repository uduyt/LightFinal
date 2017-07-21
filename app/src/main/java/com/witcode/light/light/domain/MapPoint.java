package com.witcode.light.light.domain;

import com.google.android.gms.maps.model.LatLng;

import java.util.UUID;

/**
 * Created by carlo on 17/03/2017.
 */

public class MapPoint {
    public final static int USER_ROUTE=1;
    public final static int TRANSPORT_ROUTE=2;
    public final static int BUS_STOP=3;

    public final static int WAITING_VALIDATION=-1;
    public final static int VALIDATED=-2;
    public final static int NOT_VALIDATED=-3;
    public final static int BIG_JUMP=-4;

    private String Id;
    private LatLng LatLng;
    private int Type;
    private int validated=WAITING_VALIDATION;
    private double Lights;
    private long Time;


    public MapPoint() {
        Id=UUID.randomUUID().toString();
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public com.google.android.gms.maps.model.LatLng getLatLng() {
        return LatLng;
    }

    public void setLatLng(com.google.android.gms.maps.model.LatLng latLng) {
        LatLng = latLng;
    }

    public int getType() {
        return Type;
    }

    public void setType(int type) {
        Type = type;
    }

    public int getValidated() {
        return validated;
    }

    public void setValidated(int validated) {
        this.validated = validated;
    }

    public double getLights() {
        return Lights;
    }

    public void setLights(double lights) {
        Lights = lights;
    }

    public long getTime() {
        return Time;
    }

    public void setTime(long time) {
        Time = time;
    }


    @Override
    public boolean equals(Object obj) {

        if(obj instanceof MapPoint){
            if(((MapPoint)obj).getId().equals(Id)){
                return true;
            }else{
                return false;
            }
        }else{
            return false;
        }

    }

    @Override
    public String toString() {
        return "Map point with id: " + Id + ", validated=" + validated + ", lights: " + Math.round(Lights);
    }
}
