package com.example.drivesafe;

import android.location.Location;

public class CLocation extends Location {
    private  boolean bUseMetricUnit = false;

    public CLocation(Location location){
        this(location,true);
    }

    public CLocation(Location location , boolean bUseMetricUnit){
        super(location);
        this.bUseMetricUnit = bUseMetricUnit;

    }

    public boolean getUseMetricUnits(){
        return this.bUseMetricUnit;
    }

    @Override
    public float distanceTo(Location dest) {
        float nDistance = super.distanceTo(dest);

        return nDistance;
    }

    @Override
    public double getAltitude() {
        double nAltitude = super.getAltitude();

        return nAltitude;
    }

    @Override
    public float getSpeed() {
        float nSpeed = super.getSpeed();

        return nSpeed;
    }

    @Override
    public float getAccuracy() {
        float nAccuracy = super.getAccuracy();

        return nAccuracy;
    }

    public  void setUseMetricUnits(boolean bUseMetricUnit){
        this.bUseMetricUnit = bUseMetricUnit;
    }

}
