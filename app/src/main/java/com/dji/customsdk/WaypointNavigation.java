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
        TRACING,
        SQUARE,
        POINT;
    }

    public WaypointNavigation(Mode mode, TerrainFollowing terrainFollowing,
                              double latitude, double longitude, float altitude,
                              double targetLat, double targetLong) {
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

            case TRACING:
                double lat1 = 40.571; //40.57150
                double lat2 = 40.568;
                double long1 = -74.16700;
                double long2 = -74.1620;
                double currentLat = lat1;
                double currentLong = long1;
                double deltaLat = -0.0004;
                double deltaLong = 0.0001;
                addWaypoint(latitude, longitude, terrainFollowing.getAltitude(latitude, longitude));
                while (currentLat > lat2){
                    while (currentLong < long2){
                        addWaypoint(currentLat, currentLong,
                                terrainFollowing.getAltitude(currentLat, currentLong));
                        currentLong += deltaLong;
                    }
                    currentLat += deltaLat;
                    while (currentLong > long1){
                        addWaypoint(currentLat, currentLong,
                                terrainFollowing.getAltitude(currentLat, currentLong));
                        currentLong -= deltaLong;
                    }
                    currentLat += deltaLat;
                }
                break;

            case MAPPING:
                lat1 = 40.571; //40.57150
                lat2 = 40.567;
                long1 = -74.16700;
                long2 = -74.1620;
                currentLat = lat1;
                deltaLat = -0.0004;
                float targetAltitude = 60;
                addWaypoint(latitude, longitude, altitude);
                addWaypoint(currentLat, long2, targetAltitude);
                while (currentLat > lat2){
                    addWaypoint(currentLat, long2, targetAltitude);
                    currentLat += deltaLat;
                    addWaypoint(currentLat, long1, targetAltitude);
                    currentLat += deltaLat;
                }
                break;

            case SQUARE:
                addWaypoint(latitude, longitude, altitude);
                addWaypoint(latitude + 0.0001, longitude, altitude);
                addWaypoint(latitude, longitude, altitude);
                break;

            case POINT:
                currentLat = latitude;
                currentLong = longitude;
                deltaLat = Math.abs(targetLat - latitude);
                deltaLong = Math.abs(targetLong - latitude);
                int steps = 0;
                if (Math.abs(deltaLat) > Math.abs(deltaLong)){
                    steps = (int)(deltaLat / 0.0001);
                }
                else {
                    steps = (int)(deltaLong / 0.0001);
                }
                addWaypoint(latitude, longitude, altitude);
                for (int i = 0; i < steps; i++) {
                    addWaypoint(currentLat, currentLong,
                            terrainFollowing.getAltitude(currentLat, currentLong));
                    currentLat += deltaLat / steps;
                    currentLong += deltaLong / steps;
                }
                addWaypoint(targetLat, targetLong, terrainFollowing.getAltitude(currentLat, currentLong));

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

    public float getTargetAltitude() {
        return waypoints.get(currentWaypoint).getAltitude();
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
