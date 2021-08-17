package com.dji.customsdk;

import android.content.Context;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.dji.customsdk.utils.DialogUtils;
import com.dji.customsdk.utils.ModuleVerificationUtil;

import java.util.Timer;
import java.util.TimerTask;

import dji.common.error.DJIError;
import dji.common.flightcontroller.FlightControllerState;
import dji.common.flightcontroller.GPSSignalLevel;
import dji.common.flightcontroller.LocationCoordinate3D;
import dji.common.flightcontroller.virtualstick.FlightControlData;
import dji.common.flightcontroller.virtualstick.FlightCoordinateSystem;
import dji.common.flightcontroller.virtualstick.RollPitchControlMode;
import dji.common.flightcontroller.virtualstick.VerticalControlMode;
import dji.common.flightcontroller.virtualstick.YawControlMode;
import dji.common.util.CommonCallbacks;
import dji.sdk.flightcontroller.FlightController;

public class VirtualSticks extends RelativeLayout
    implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private boolean yawControlModeFlag = true;
    private boolean rollPitchControlModeFlag = true;
    private boolean verticalControlModeFlag = true;
    private boolean horizontalCoordinateFlag = true;

    private TextView textView;

    private Timer sendVirtualStickDataTimer;
    private SendVirtualStickDataTask sendVirtualStickDataTask;
    private Timer UpdateGUITimer;
    private UpdateGUITask updateGUITask;
    private Context context;
    private CameraImaging cameraImaging;
    private WaypointNavigation waypointNavigation;
    private float pitch;
    private float roll;
    private float yaw;
    private float throttle;
    private float pitchVelocity;
    private float angularVelocity;
    private float rollVelocity;
    private float targetAltitude;
    private double latitude;
    private double longitude;
    private float altitude;
    private int internalTimer;

    // Enum that tracks which mode the drone is in
    private enum Mode {
        OFF,
        SPIN,
        ORBIT,
        FORWARD,
        BACKWARD,
        WAYPOINT
    }
    private Mode mode;

    public VirtualSticks(Context context) {
        super(context);
        this.context = context;
        cameraImaging = ((MainActivity) context).getCamera();
        updateGUITask = new UpdateGUITask();
        UpdateGUITimer = new Timer();
        UpdateGUITimer.schedule(updateGUITask, 1000, 50);
    }

    public MainActivity getMainContext(){
        return (MainActivity)getContext();
    }

    @Override
    public void onClick(View v) {
        // Handles button presses for VirtualSticks
        switch (v.getId()) {

            case R.id.btn_disable_virtual_stick:
                disableVirtualSticks();
                break;
            case R.id.btn_spin:
                // Checks if the virtual sticks are on or not
                enableVirtualSticks();
                mode = mode.SPIN;
                if (cameraImaging != null && cameraImaging.isCameraAvailable()){
                    System.out.println("Starting camera");
                    cameraImaging.startCameraTimer(100, 5000);
                }
                break;
            case R.id.btn_orbit:
                // Checks if the virtual sticks are on or not
                enableVirtualSticks();
                mode = mode.BACKWARD;
                break;
            case R.id.btn_waypoint:
                if (!isGPSStrong()){
                    break;
                }
                enableWaypoints();
                waypointNavigation = new WaypointNavigation(
                        WaypointNavigation.Mode.MAPPING, getMainContext().getTerrainFollowing(),
                        latitude, longitude, altitude);
//                if (cameraImaging != null && cameraImaging.isCameraAvailable()){
//                    System.out.println("Starting camera, 2 sec interval");
//                    cameraImaging.startCameraTimer(800, 2200);
//                }
                mode = mode.WAYPOINT;
                targetAltitude = altitude;
                break;
            default:
                break;
        }
    }

    // Handles sliders
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
                                  boolean fromUser) {
        System.out.println(progress);
        switch (seekBar.getId()) {

            case R.id.seekbar_rollvelocity:
                pitchVelocity = (float)(((progress - 50) / 5) * 0.1);
//                getMainContext().textPitchVelocity.setText(progress);
                getMainContext().textRollVelocity.setText("Roll velocity: " + rollVelocity );
                break;

            case R.id.seekbar_angularvelocity:
                angularVelocity = (float)(((progress - 50) / 2) * 2);
//                getMainContext().textAngularVelocity.setText(progress);
                getMainContext().textAngularVelocity.setText("Angular velocity: " + angularVelocity );
                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {}

    // Checks GPS is strong enough to do flight with waypoints
    public boolean isGPSStrong(){
        FlightController flightController = ModuleVerificationUtil.getFlightController();
        if (flightController == null) {
            return false;
        }
        FlightControllerState flightControllerState = flightController.getState();
        if (flightController.getState() == null){
            return false;
        }
        GPSSignalLevel GPS = flightControllerState.getGPSSignalLevel();
        if (GPS == null){
            return false;
        }
        switch (GPS){
            case NONE:
            case LEVEL_0:
            case LEVEL_1:
            case LEVEL_2:
            case LEVEL_3:
                getMainContext().showToast("GPS too weak");
                return false;
            default:
                return true;
        }
    }

    // Virtual sticks functionality

    public void enableVirtualSticks() {
        // Enable sticks
        FlightController flightController = ModuleVerificationUtil.getFlightController();
        if (flightController == null) {
            getMainContext().showToast("Could not get flight controller");
            return;
        }
        flightController.setVirtualStickModeEnabled(true, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                DialogUtils.showDialogBasedOnError(context, djiError);
//                        Toast.makeText(context,"Failed to activate virtual sticks",Toast.LENGTH_SHORT).show();
            }
        });
        // And advanced sticks
        flightController.setVirtualStickAdvancedModeEnabled(true);
        // Make the timer
        if (null == sendVirtualStickDataTimer) {
            sendVirtualStickDataTask = new SendVirtualStickDataTask();
            sendVirtualStickDataTimer = new Timer();
            sendVirtualStickDataTimer.schedule(sendVirtualStickDataTask, 100, 200);
        }
        // Set all modes and coordinate systems to proper values
        flightController.setRollPitchControlMode(RollPitchControlMode.VELOCITY);
        flightController.setYawControlMode(YawControlMode.ANGULAR_VELOCITY);
        flightController.setVerticalControlMode(VerticalControlMode.VELOCITY);
        flightController.setRollPitchCoordinateSystem(FlightCoordinateSystem.BODY);
        cameraImaging.stopCameraTimer();
    }

    public void disableVirtualSticks() {
        FlightController flightController = ModuleVerificationUtil.getFlightController();
        if (flightController == null) {
            return;
        }
        // Disable sticks
        flightController.setVirtualStickModeEnabled(false, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                DialogUtils.showDialogBasedOnError(context, djiError);
            }
        });
        flightController.setVirtualStickAdvancedModeEnabled(false);
//                Toast.makeText(context,"Deactivated virtual sticks",Toast.LENGTH_SHORT).show();
        System.out.println("Sticks deactivated");
        // Make sure you kill the timer
        if (null != sendVirtualStickDataTimer) {
            sendVirtualStickDataTimer.cancel();
            sendVirtualStickDataTimer = null;
            sendVirtualStickDataTask = null;
        }
        // Set the mode to off
        mode = Mode.OFF;
        cameraImaging.stopCameraTimer();
    }

    public void enableWaypoints() {
        FlightController flightController = ModuleVerificationUtil.getFlightController();
        if (flightController == null) {
            return;
        }
        // Enable sticks
        flightController.setVirtualStickModeEnabled(true, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                DialogUtils.showDialogBasedOnError(context, djiError);
//                        Toast.makeText(context,"Failed to activate virtual sticks",Toast.LENGTH_SHORT).show();
            }
        });
        // And advanced sticks
        flightController.setVirtualStickAdvancedModeEnabled(true);
        // Make the timer
        if (null == sendVirtualStickDataTimer) {
            sendVirtualStickDataTask = new SendVirtualStickDataTask();
            sendVirtualStickDataTimer = new Timer();
            sendVirtualStickDataTimer.schedule(sendVirtualStickDataTask, 100, 200);
        }
        // Set all modes and coordinate systems to proper values
        flightController.setRollPitchControlMode(RollPitchControlMode.VELOCITY);
        flightController.setYawControlMode(YawControlMode.ANGLE);
        flightController.setVerticalControlMode(VerticalControlMode.POSITION);
        flightController.setRollPitchCoordinateSystem(FlightCoordinateSystem.BODY);
        cameraImaging.stopCameraTimer();
    }

    // Updates pitch, roll, yaw, throttle based on the mode. Pitch, roll, and throttle are in m/s.
    // Yaw is in degrees/s.
    public void updateFlightControlData() {
        switch(mode) {
            case WAYPOINT:
                if (!isGPSStrong()){
                    getMainContext().showToast("GPS is not strong enough");
                    break;
                }
                if (waypointNavigation == null){
                    getMainContext().showToast("No waypoints");
                    break;
                }
                else if (waypointNavigation.isCloseToWaypoint(latitude, longitude, 0.000006)){
                    waypointNavigation.currentWaypoint++;
                    if (cameraImaging != null && cameraImaging.isCameraAvailable()){
                        cameraImaging.takePhoto();
                    }
                    if (!waypointNavigation.hasNextHeading()){
                        disableVirtualSticks();
                        waypointNavigation = null;
                        resetMotion();
                        break;
                    }
                }
//                else if (waypointNavigation.isOvershootingWaypoint(latitude, longitude)){
//                    waypointNavigation.insertWaypoint(latitude, longitude, altitude);
//                }
                yaw = waypointNavigation.getNextHeading(latitude, longitude);
//                getMainContext().showToast("Angle:" + yaw);
                if (Math.abs(waypointNavigation.getTargetAltitude() - altitude) > 5) {
                    pitch = (float)1;
                }
                else if (waypointNavigation.isCloseToWaypoint(latitude, longitude, 0.00002)){
                    pitch = (float)2;
                }
                else {
                    pitch = (float)4.5;
                }
                throttle = waypointNavigation.getTargetAltitude();
                break;

            case BACKWARD:
                internalTimer++;
                roll = -1;
                double rollTimer = 360 * 5 * rollVelocity / angularVelocity / 2 / Math.PI;
                if (internalTimer > rollTimer) {
                    if (cameraImaging != null){
                        System.out.println("Starting camera");
                        cameraImaging.startCameraTimer(100, 2000);
                    }
                    internalTimer = 0;
                    mode = mode.ORBIT;
                }
                break;
            case ORBIT:
            case SPIN:
                roll = 0;
                yaw = angularVelocity;
                roll = rollVelocity;
                break;
            default:
                resetMotion();
                break;
        }
    }

    private void resetMotion() {
        pitch = 0;
        roll = 0;
        yaw = 0;
        throttle = 0;
    }
    // Sends the flight data (pitch, roll, yaw, throttle) to the flight controller.
    private class SendVirtualStickDataTask extends TimerTask {

        @Override
        public void run() {
            FlightController flightController = ModuleVerificationUtil.getFlightController();
            if (flightController == null) {
                return;
            }
            updateFlightControlData();
//            System.out.println(String.format("%.2f, %.2f, %.2f, %.2f", pitch, roll, yaw, throttle));
            flightController.sendVirtualStickFlightControlData(new FlightControlData(roll, pitch, yaw, throttle),
                                new CommonCallbacks.CompletionCallback() {
                                    @Override
                                    public void onResult(DJIError djiError) {
                                    }
                                });
        }
    }

    private class UpdateGUITask extends TimerTask {
        final Runnable GUIRunnable = new Runnable() {
            public void run() {
                int currentWaypoint = 0;
                int numWaypoints = 0;
                double deltaLat = 0;
                double deltaLong = 0;
                double deltaAlt = 0;
                float projectedAltitude = getMainContext().getTerrainFollowing().getAltitude(latitude, longitude);
                if (mode == Mode.WAYPOINT && waypointNavigation != null) {
                    currentWaypoint = waypointNavigation.currentWaypoint;
                    numWaypoints = waypointNavigation.numWaypoints();
                    deltaLat = waypointNavigation.getTargetLatitude() - latitude;
                    deltaLong = waypointNavigation.getTargetLongitude() - longitude;
                    deltaAlt = waypointNavigation.getTargetAltitude() - altitude;
                    getMainContext().textLatitudeLongitude.setText(
                            "Latitude: " + latitude + "Longitude: " + longitude + "Yaw: " + yaw
                                    + "Waypoint:" + currentWaypoint + "/" + numWaypoints + "Delta:" + deltaLat + ", " + deltaLong
                                    + ", " + deltaAlt + "Projected altitude: " + projectedAltitude);
                }
                else {
                    getMainContext().textLatitudeLongitude.setText(
                            "Latitude: " + latitude + "Longitude: " + longitude + "Yaw: " + yaw);
                }
            }
        };

        @Override
        public void run() {
            FlightController flightController = ModuleVerificationUtil.getFlightController();
            if (flightController != null) {
            FlightControllerState flightControllerState = flightController.getState();
                if (flightControllerState != null) {
                    LocationCoordinate3D location = flightControllerState.getAircraftLocation();
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        altitude = location.getAltitude();
                        getMainContext().handler.post(GUIRunnable);
                    }
                }
            }
        }
    }

}
