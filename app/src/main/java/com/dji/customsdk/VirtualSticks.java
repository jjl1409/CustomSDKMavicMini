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
    private double latitude;
    private double longitude;
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
        cameraImaging = ((MainActivity)context).getCamera();
        updateGUITask= new UpdateGUITask();
        UpdateGUITimer = new Timer();
        UpdateGUITimer.schedule(updateGUITask, 1000, 50);
    }


    @Override
    public void onClick(View v) {
        // Handles button presses for VirtualSticks

        FlightController flightController = ModuleVerificationUtil.getFlightController();
        if (flightController == null) {
            return;
        }
        switch (v.getId()) {

            case R.id.btn_disable_virtual_stick:
                disableVirtualSticks(flightController);

            case R.id.btn_spin:
                // Checks if the virtual sticks are on or not
                enableVirtualSticks(flightController);
                mode = mode.SPIN;
                if (cameraImaging != null){
                    System.out.println("Starting camera");
                    cameraImaging.startCameraTimer(100, 5000);
                }
                break;
            case R.id.btn_orbit:
                // Checks if the virtual sticks are on or not
                enableVirtualSticks(flightController);
                mode = mode.BACKWARD;
                break;
            case R.id.btn_waypoint:
                enableVirtualSticks(flightController);
                mode = mode.WAYPOINT;
                if (mode != mode.OFF) {
                    mode = mode.WAYPOINT;
                }
            default:
                break;
        }
    }

    // Handles sliders
    public void onProgressChanged(SeekBar seekBar, int progress,
                                  boolean fromUser) {
        switch (seekBar.getId()) {

            case R.id.seekbar_pitchvelocity:
                pitchVelocity = (float)(((progress - 50) / 5) * 0.1);
                ((MainActivity)getContext()).textPitchVelocity.setText("Pitch velocity: " + pitchVelocity );
                break;

            case R.id.seekbar_angularvelocity:
                angularVelocity = (float)(((progress - 50) / 2) * 2);
                ((MainActivity)getContext()).textAngularVelocity.setText("Angular velocity: " + angularVelocity );
                break;
        }
    }

    public void onStartTrackingTouch(SeekBar seekBar) {}

    public void onStopTrackingTouch(SeekBar seekBar) {}


    // Virtual sticks functionality

    public void enableVirtualSticks(FlightController flightController) {
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
        flightController.setYawControlMode(YawControlMode.ANGULAR_VELOCITY);
        flightController.setVerticalControlMode(VerticalControlMode.VELOCITY);
        flightController.setRollPitchCoordinateSystem(FlightCoordinateSystem.BODY);
        cameraImaging.stopCameraTimer();
    }

    public void disableVirtualSticks(FlightController flightController) {
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
        if (null == sendVirtualStickDataTimer) {
            sendVirtualStickDataTimer.cancel();
            sendVirtualStickDataTimer = null;
            sendVirtualStickDataTask = null;
        }
        // Set the mode to off
        cameraImaging.stopCameraTimer();
    }

    public void enableWaypoints(FlightController flightController) {
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
        flightController.setYawControlMode(YawControlMode.ANGULAR_VELOCITY);
        flightController.setVerticalControlMode(VerticalControlMode.VELOCITY);
        flightController.setRollPitchCoordinateSystem(FlightCoordinateSystem.BODY);
        cameraImaging.stopCameraTimer();

    }

    // Updates pitch, roll, yaw, throttle based on the mode. Pitch, roll, and throttle are in m/s.
    // Yaw is in degrees/s.
    public void updateFlightControlData() {
        switch(mode) {
            case WAYPOINT:
            case BACKWARD:
                internalTimer++;
                roll = -1;
                double rollTimer = 360 * 5 * pitchVelocity / angularVelocity / 2 / Math.PI;
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
                pitch = pitchVelocity;
                break;
            default:
                pitch = 0;
                roll = 0;
                yaw = 0;
                throttle = 0;
                break;
        }
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
            flightController.sendVirtualStickFlightControlData(new FlightControlData(pitch, roll, yaw, throttle),
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
                ((MainActivity) getContext()).textLatitudeLongitude.setText(
                        "Latitude:" + latitude + "Longitude:" + longitude);
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
                        ((MainActivity) getContext()).handler.post(GUIRunnable);
                    }
                }
            }
        }
    }

}
