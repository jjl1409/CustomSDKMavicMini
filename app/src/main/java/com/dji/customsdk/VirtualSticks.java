package com.dji.customsdk;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.annotation.NonNull;
import android.os.Bundle;

import com.dji.customsdk.NewApplication;
import com.dji.customsdk.R;
import com.dji.customsdk.CameraImaging;
import com.dji.customsdk.utils.DialogUtils;
import com.dji.customsdk.utils.ModuleVerificationUtil;


import java.util.Timer;
import java.util.TimerTask;

import dji.common.error.DJIError;
import dji.common.flightcontroller.virtualstick.FlightControlData;
import dji.common.flightcontroller.virtualstick.FlightCoordinateSystem;
import dji.common.flightcontroller.virtualstick.RollPitchControlMode;
import dji.common.flightcontroller.virtualstick.VerticalControlMode;
import dji.common.flightcontroller.virtualstick.YawControlMode;
import dji.common.model.LocationCoordinate2D;
import dji.common.util.CommonCallbacks;
import dji.keysdk.FlightControllerKey;
import dji.keysdk.KeyManager;
import dji.sdk.flightcontroller.FlightController;

public class VirtualSticks extends RelativeLayout
    implements View.OnClickListener{

    private boolean yawControlModeFlag = true;
    private boolean rollPitchControlModeFlag = true;
    private boolean verticalControlModeFlag = true;
    private boolean horizontalCoordinateFlag = true;

    private TextView textView;

    private Timer sendVirtualStickDataTimer;
    private SendVirtualStickDataTask sendVirtualStickDataTask;
    private Context context;
    private CameraImaging cameraImaging;
    private float pitch;
    private float roll;
    private float yaw;
    private float throttle;

    // Enum that tracks which mode the drone is in
    private enum Mode {
        ON,
        OFF,
        SPIN,
        ORBIT
    }
    private Mode mode;

    public VirtualSticks(Context context) {
        super(context);
        this.context = context;
        cameraImaging = ((MainActivity)context).getCamera();
    }

    @Override
    public void onClick(View v) {
        // Handles button presses for VirtualSticks

        FlightController flightController = ModuleVerificationUtil.getFlightController();
        if (flightController == null) {
            return;
        }
        switch (v.getId()) {
            case R.id.btn_enable_virtual_stick:
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
//                Toast.makeText(context,"Activated virtual sticks",Toast.LENGTH_SHORT).show();
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
                // Set the mode to ON
                mode = mode.ON;
                cameraImaging.stopCameraTimer();
                break;

            case R.id.btn_disable_virtual_stick:
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
                mode = mode.OFF;
                cameraImaging.stopCameraTimer();
                break;

            case R.id.btn_spin:
                // Checks if the virtual sticks are on or not
                if (mode != mode.OFF) {
                    mode = mode.SPIN;
                    if (cameraImaging != null){
                        System.out.println("Starting camera");
                        cameraImaging.startCameraTimer(100, 5000);
                    }
                }
                break;
            case R.id.btn_orbit:
                // Checks if the virtual sticks are on or not
                if (mode != mode.OFF) {
                    mode = mode.ORBIT;
                    if (cameraImaging != null){
                        System.out.println("Starting camera");
                        cameraImaging.startCameraTimer(100, 2000);
                    }
                }
                break;
            default:
                break;
        }
    }

    // Updates pitch, roll, yaw, throttle based on the mode. Pitch, roll, and throttle are in m/s.
    // Yaw is in degrees/s. (TBD)
    public void updateFlightControlData() {
        switch(mode) {
            case ORBIT:
                yaw = 0;
                pitch = (float)0.1;
                break;
            case SPIN:
                yaw = 40;
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

}
