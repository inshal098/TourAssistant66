package com.tourassistant.coderoids.helpers;

import android.location.Location;

/**
 * Created by developer on 11/11/2015.
 */
public class LocationHelper {

    private String latitude = "", longitude = "", bearing = "", speed = "", gpsCoordinate = "";
    private Location location = null;
    public static long previousTimeRecord;
    public static long currentTimeRecord;

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getBearing() {
        return bearing;
    }

    public void setBearing(String bearing) {
        this.bearing = bearing;
    }

    private static LocationHelper ourInstance = new LocationHelper();

    public static LocationHelper getInstance() {
        return ourInstance;
    }

    private LocationHelper() {
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getGpsCoordinate() {
        return gpsCoordinate;
    }

    public void setGpsCoordinate(String gpsCoordinate) {
        this.gpsCoordinate = gpsCoordinate;
    }
}
