package com.witcode.light.light.domain;

import com.google.android.gms.maps.model.PolylineOptions;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by rosety on 22/9/17.
 */

public class ActivityObject{
    private boolean isRunning = false;
    private int Type;
    private long Millis;
    private float Speed;
    private double Lights, Distance;
    private boolean CercaniasOrUrban = false, IsTooFast=false;
    private ArrayList<PolylineOptions> UserRoutePolylines=new ArrayList<>();
    private ArrayList<PolylineOptions> InitialRoutePolylines=new ArrayList<>();
    private ArrayList<MapPoint> UserRoutePoints=new ArrayList<>();
    private String Line;

    public ActivityObject() {
    }

    public int getType() {
        return Type;
    }

    public void setType(int type) {
        Type = type;
    }

    public boolean isCercaniasOrUrban() {
        return CercaniasOrUrban;
    }

    public void setCercaniasOrUrban(boolean cercaniasOrUrban) {
        CercaniasOrUrban = cercaniasOrUrban;
    }

    public ArrayList<PolylineOptions> getUserRoutePolylines() {
        return UserRoutePolylines;
    }

    public void setUserRoutePolylines(ArrayList<PolylineOptions> userRoutePolylines) {
        UserRoutePolylines = userRoutePolylines;
    }

    public void addRoutePolylineOption(PolylineOptions polylineOptions) {
        UserRoutePolylines.add(polylineOptions);
    }


    public ArrayList<MapPoint> getUserRoutePoints() {
        return UserRoutePoints;
    }

    public void setUserRoutePoints(ArrayList<MapPoint> userRoutePoints) {
        UserRoutePoints = userRoutePoints;
    }

    public void addRouteMapPoint(MapPoint mapPoint) {
        UserRoutePoints.add(mapPoint);
    }


    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }

    public double getDistance() {
        return Distance;
    }

    public void setDistance(double distance) {
        Distance = distance;
    }

    public void addToDistance(double distance) {
        Distance += distance;
    }

    public long getMillis() {
        return Millis;
    }

    public float getSpeed() {
        return Speed;
    }

    public void setSpeed(float speed) {
        Speed = speed;
    }

    public double getLights() {
        return Lights;
    }

    public void setLights(double lights) {
        Lights = lights;
    }

    public void addToLights(double lights) {
        Lights += lights;
    }

    public String getLine() {
        return Line;
    }

    public void setLine(String line) {
        Line = line;
    }

    public long getSeconds() {
        return (System.currentTimeMillis() - Millis) / 1000;
    }

    public void InitiateMillis() {
        Millis = System.currentTimeMillis();
    }

    public void setMillis(long millis) {
        Millis = millis;
    }

    public ArrayList<PolylineOptions> getInitialRoutePolylines() {
        return InitialRoutePolylines;
    }

    public void setInitialRoutePolylines(ArrayList<PolylineOptions> initialRoutePolylines) {
        InitialRoutePolylines = initialRoutePolylines;
    }

    public void addToInitialRoutePolylines(PolylineOptions polylineOptions) {
        InitialRoutePolylines.add(polylineOptions);
    }

    public String getTimeString(){
        long Seconds=getSeconds();
        int minutes = (int) (Seconds) / 60;
        int hours = minutes / 60;

        String shours, sminutes, sseconds;

        shours = hours < 10 ? "0" + String.valueOf(hours) : String.valueOf(hours);
        sminutes = (minutes % 60) < 10 ? "0" + String.valueOf(minutes % 60) : String.valueOf(minutes % 60);
        sseconds = (Seconds % 60) < 10 ? "0" + String.valueOf(Seconds % 60) : String.valueOf(Seconds % 60);

        return shours + ":" + sminutes + ":" + sseconds;
    }

    public boolean isTooFast() {
        return IsTooFast;
    }

    public void setTooFast(boolean tooFast) {
        IsTooFast = tooFast;
    }


    public final static int ACTIVITY_NONE = 0;
    public final static int ACTIVITY_WALK = 1;
    public final static int ACTIVITY_BIKE = 2;
    public final static int ACTIVITY_BUS = 3;
    public final static int ACTIVITY_RAILROAD = 4;
    public final static int ACTIVITY_CARSHARE = 5;
    public final static int ACTIVITY_RECYCLE = 6;

    public String getTypeString(){
        String res;

        switch (getType()){
            case 1:
                res="walk";
                break;
            case 2:
                res="bike";
                break;
            case 3:
                res="bus";
                break;
            case 4:
                res="railroad";
                break;
            case 5:
                res="carshare";
                break;
            case 6:
                res="recycle";
                break;
            default:
                res="null";
                break;
        }

        return res;
    }
}
