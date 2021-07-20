package com.dji.customsdk;

import java.util.ArrayList;

import dji.common.flightcontroller.LocationCoordinate3D;

public class WaypointNavigation {
    private ArrayList<LocationCoordinate3D> waypoints;
    private int currentWaypoint;

    public void addWaypoint(LocationCoordinate3D location) {
        waypoints.add(location);
    }

    public void insertWaypoint(LocationCoordinate3D location) {
        waypoints.add(currentWaypoint, location);
        currentWaypoint++;
    };

    public void removeWaypoint(int index) {
        waypoints.remove(index);
    }

    public boolean hasNextHeading() {
        return currentWaypoint < waypoints.size();
    }

    public double getNextHeading(double lat1, double long1) {
        double lat2 = waypoints.get(currentWaypoint).getLatitude();
        double long2 = waypoints.get(currentWaypoint).getLongitude();
        currentWaypoint++;
        double x = Math.cos(lat2) * Math.sin(long2 - long1);
        double y = Math.cos(lat1) * Math.sin(lat2)
                 - Math.sin(lat1) * Math.cos(lat2) * Math.cos(long2 - long1);
        return Math.atan2(x, y);
    }

    public boolean isCloseToWaypoint(double lat1, double long1) {
        double lat2 = waypoints.get(currentWaypoint).getLatitude();
        double long2 = waypoints.get(currentWaypoint).getLongitude();
        boolean closeToWayPoint = Math.abs(long2 - long1) < 0.00001
                               && Math.abs(lat2 - lat1) < 0.00001;
        if (closeToWayPoint) {
            currentWaypoint++;
        }
        return closeToWayPoint;
    }

    public boolean isOvershootingWaypoint(double lat1, double long1){
        double latCalculatedDelta = waypoints.get(currentWaypoint).getLatitude()
                        - waypoints.get(currentWaypoint - 1).getLatitude();
        double longCalculatedDelta= waypoints.get(currentWaypoint).getLongitude()
                        - waypoints.get(currentWaypoint - 1).getLongitude();
        double latActualDelta = lat1 - waypoints.get(currentWaypoint).getLatitude();
        double longActualDelta= long1 - waypoints.get(currentWaypoint).getLongitude();
        return latActualDelta * latCalculatedDelta < 0 || longActualDelta * longCalculatedDelta < 0;
    }
}
