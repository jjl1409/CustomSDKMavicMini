package com.dji.customsdk;

import android.location.Location;

import java.util.ArrayList;

import dji.common.flightcontroller.LocationCoordinate3D;

public class WaypointNavigation {
    private ArrayList<LocationCoordinate3D> waypoints;
    public int currentWaypoint;

    public enum Mode {
        COMPLICATED,
        MAPPING,
        SQUARE;
    }

    public WaypointNavigation(Mode mode, double latitude, double longitude, float altitude) {
        waypoints = new ArrayList<LocationCoordinate3D>();
        switch (mode) {
            case COMPLICATED:
                addWaypoint(latitude, longitude, altitude);
                addWaypoint(latitude + 0.0001, longitude, altitude);
                addWaypoint(latitude + 0.0001, longitude + 0.0001, altitude);
                addWaypoint(latitude , longitude + 0.0001, altitude);
                addWaypoint(latitude - 0.0001, longitude + 0.0001, altitude);
                addWaypoint(latitude - 0.0001, longitude, altitude);
                addWaypoint(latitude - 0.0001, longitude - 0.0001, altitude);
                addWaypoint(latitude , longitude - 0.0001, altitude);
                addWaypoint(latitude + 0.0001, longitude - 0.0001, altitude);
                addWaypoint(latitude + 0.0001, longitude, altitude);
                addWaypoint(latitude, longitude, altitude);
                break;
            case MAPPING:
                double lat1 = 40.56950; //40.57150
                double lat2 = 40.56600;
                double long1 = -74.16700;
                double long2 = -74.1600;
                double currentLat = lat1;
                double deltaLat = -0.0005;
                addWaypoint(latitude, longitude, altitude);
                while (currentLat > lat2){
                    addWaypoint(currentLat, long1, altitude);
                    addWaypoint(currentLat, long2, altitude);
                    currentLat += deltaLat;
                    addWaypoint(currentLat, long2, altitude);
                    addWaypoint(currentLat, long1, altitude);
                    currentLat += deltaLat;
                }
                break;
            case SQUARE:
                addWaypoint(latitude, longitude, altitude);
                addWaypoint(latitude + 0.0001, longitude, altitude);
                addWaypoint(latitude, longitude, altitude);
                break;
        }
        currentWaypoint = 1;

    }

    public int numWaypoints() {
        return waypoints.size();
    }
    public void addWaypoint(double latitude, double longitude, float altitude) {
        waypoints.add(new LocationCoordinate3D(latitude, longitude, altitude));
    }

    public void insertWaypoint(double latitude, double longitude, float altitude) {
        waypoints.add(new LocationCoordinate3D(latitude, longitude, altitude));
        currentWaypoint++;
    };

    public void removeWaypoint(int index) {
        waypoints.remove(index);
    }

    public boolean hasNextHeading() {
        return currentWaypoint < waypoints.size();
    }

    public double getTargetLatitude() {
        return waypoints.get(currentWaypoint).getLatitude();
    }
    public double getTargetLongitude() {
        return waypoints.get(currentWaypoint).getLongitude();
    }
    public float getNextHeading(double lat1, double long1) {
        double lat2 = getTargetLatitude();
        double long2 = getTargetLongitude();
//        currentWaypoint++;
        double x = Math.cos(lat2) * Math.sin(long2 - long1);
        double y = (Math.cos(lat1) * Math.sin(lat2))
                 - (Math.sin(lat1) * Math.cos(lat2) * Math.cos(long2 - long1));
        return -(float)Math.toDegrees(Math.atan2(x, y));
    }

    public boolean isCloseToWaypoint(double lat1, double long1, double tolerance) {
        double lat2 = getTargetLatitude();
        double long2 = getTargetLongitude();
        boolean closeToWayPoint = Math.abs(long2 - long1) < tolerance
                               && Math.abs(lat2 - lat1) < tolerance;
        return closeToWayPoint;
    }


    public boolean isOvershootingWaypoint(double lat1, double long1){
        double latCalculatedDelta = getTargetLatitude()
                        - waypoints.get(currentWaypoint - 1).getLatitude();
        double longCalculatedDelta= getTargetLongitude()
                        - waypoints.get(currentWaypoint - 1).getLongitude();
        double latActualDelta = lat1 - getTargetLatitude();
        double longActualDelta = long1 - waypoints.get(currentWaypoint).getLongitude();
        return latActualDelta * latCalculatedDelta < 0 || longActualDelta * longCalculatedDelta < 0;
    }

}
