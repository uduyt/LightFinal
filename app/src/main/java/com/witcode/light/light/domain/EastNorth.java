package com.witcode.light.light.domain;

/**
 * Created by rosety on 1/6/17.
 */

public class EastNorth {

    private double Easting;
    private double Northing;

    public EastNorth(double easting, double northing) {
        Easting = easting;
        Northing = northing;
    }

    public double getEasting() {
        return Easting;
    }

    public void setEasting(double easting) {
        Easting = easting;
    }

    public double getNorthing() {
        return Northing;
    }

    public void setNorthing(double northing) {
        Northing = northing;
    }
}
