package com.dji.customsdk;

import android.content.Context;
import android.content.res.Resources;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.dji.customsdk.utils.ModuleVerificationUtil;

import java.util.Timer;
import java.util.TimerTask;

import dji.common.flightcontroller.FlightControllerState;
import dji.common.flightcontroller.LocationCoordinate3D;
import dji.sdk.flightcontroller.FlightController;

public class MapGUI extends RelativeLayout
implements View.OnTouchListener, View.OnClickListener{

    private MainActivity mainContext;
    private View view;
    private VirtualSticks virtualSticks;
    float windowWidth= 1280;
    float windowHeight = 736;
    float actualHeight = 601;
    float actualWidth = 962;
    double left = -74.1673479;
    double right = -74.1599551;
    double top = 40.5718389;
    double bot = 40.5682222;
    float density;
    double droneLatitude;
    double droneLongitude;
    private Timer droneLocationTimer;
    private DroneLocationTask sendDroneLocationTask;

    public MapGUI(Context context) {
        super(context);
        this.mainContext = (MainActivity)context;
        view = mainContext.map;
        virtualSticks = mainContext.getVirtualSticks();
        density = getResources().getDisplayMetrics().density;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_map:
                mainContext.map.setVisibility(View.VISIBLE);
                break;
            case R.id.btn_return:
                mainContext.map.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            float x = event.getRawX();
            float y = event.getRawY();
            float offsetX = -12.5f * density;
//            float offsetY = 36.5f * density;
            float offsetY = -37.5f * density;
//            float y = Resources.getSystem().getDisplayMetrics().widthPixels;
//            float x = Resources.getSystem().getDisplayMetrics().heightPixels;
//            mainContext.showToast("x: " + x + " y: " + y);
            x = (x + offsetX);
            y = (y + offsetY);
            if (virtualSticks.mode != VirtualSticks.Mode.WAYPOINT) {
                createWaypoints(getLongitudeFromX(x), getLatitudeFromY(y));
                mainContext.target.setVisibility(View.VISIBLE);
                mainContext.target.setX(x);
                mainContext.target.setY(y);
            }
            return true;
        }
        else {
            return false;
        }
    }

    public void createWaypoints(double latitude, double longitude) {
        if (!virtualSticks.isGPSStrong()){
            return;
        }
        virtualSticks.enableWaypoints();
        virtualSticks.waypointNavigation = new WaypointNavigation(
            WaypointNavigation.Mode.TRACING, mainContext.getTerrainFollowing(),
            latitude, longitude, virtualSticks.altitude, latitude, longitude);
        virtualSticks.mode = virtualSticks.mode.WAYPOINT;
        return;
    }


    public double getLongitudeFromX(float x) {
        double scaledX = (x / density - 82) / 800;
        return left + (right - left) * scaledX;
    }

    public double getLatitudeFromY(float y){
        double scaledY = (y / density - 25 / 400);
        return top + (top - bot) * scaledY;
    }

    public float getXFromLongitude(double longitude){
        double scaledX = (longitude - left) / (right - left);
        return (float)(scaledX * 800 + 82);

    }

    public float getYFromLatitude(double latitude){
        double scaledY = (latitude - top) / (bot - top);
        return (float)(scaledY * 400 + 25);

    }


    private class DroneLocationTask extends TimerTask {
        final Runnable DroneLocationRunnable = new Runnable() {
            public void run() {
                float offsetX = -12.5f * density;
                float offsetY = -37.5f * density;
                mainContext.drone.setVisibility(VISIBLE);
                mainContext.drone.setX(getXFromLongitude(droneLongitude));
                mainContext.drone.setY(getYFromLatitude(droneLatitude));
            }
        };

        @Override
        public void run() {
            if (!virtualSticks.isGPSStrong()){
                cancel();
            }
            FlightController flightController = ModuleVerificationUtil.getFlightController();
            if (flightController != null) {
                FlightControllerState flightControllerState = flightController.getState();
                if (flightControllerState != null) {
                    LocationCoordinate3D location = flightControllerState.getAircraftLocation();
                    if (location != null) {
                        droneLatitude = location.getLatitude();
                        droneLongitude = location.getLatitude();
                        mainContext.handler.post(DroneLocationRunnable);
                        return;
                    }
                }
            }
            cancel();
        }
    }
}
