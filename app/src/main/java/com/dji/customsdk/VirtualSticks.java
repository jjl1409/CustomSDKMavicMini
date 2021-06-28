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
import com.dji.customsdk.utils.DialogUtils;
import com.dji.customsdk.utils.ModuleVerificationUtil;


import java.util.Timer;
import java.util.TimerTask;

import dji.common.error.DJIError;
import dji.common.flightcontroller.simulator.InitializationData;
import dji.common.flightcontroller.simulator.SimulatorState;
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
    private float pitch;
    private float roll;
    private float yaw;
    private float throttle;

    public VirtualSticks(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public void onClick(View v) {

        FlightController flightController = ModuleVerificationUtil.getFlightController();
        if (flightController == null) {
            return;
        }
        switch (v.getId()) {
            case R.id.btn_enable_virtual_stick:
                flightController.setVirtualStickModeEnabled(true, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        DialogUtils.showDialogBasedOnError(context, djiError);
//                        Toast.makeText(context,"Failed to activate virtual sticks",Toast.LENGTH_SHORT).show();
                    }
                });
                flightController.setVirtualStickAdvancedModeEnabled(true);
//                Toast.makeText(context,"Activated virtual sticks",Toast.LENGTH_SHORT).show();
                flightController.getVirtualStickModeEnabled(new CommonCallbacks.CompletionCallbackWith<Boolean>() {
                    @Override
                    public void onSuccess(Boolean aBoolean) {
                        System.out.println("Sticks on");
                    }

                    @Override
                    public void onFailure(DJIError djiError) {
                        System.out.println("Sticks off");

                    }
                });
                if (null == sendVirtualStickDataTimer) {
                    sendVirtualStickDataTask = new SendVirtualStickDataTask();
                    sendVirtualStickDataTimer = new Timer();
                    sendVirtualStickDataTimer.schedule(sendVirtualStickDataTask, 100, 200);
                }
                break;

            case R.id.btn_disable_virtual_stick:
                flightController.setVirtualStickModeEnabled(false, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        DialogUtils.showDialogBasedOnError(context, djiError);
                    }
                });
                flightController.setVirtualStickAdvancedModeEnabled(false);
//                Toast.makeText(context,"Deactivated virtual sticks",Toast.LENGTH_SHORT).show();
                System.out.println("Sticks deactivated");
                if (null == sendVirtualStickDataTimer) {
                    sendVirtualStickDataTimer.cancel();
                    sendVirtualStickDataTimer = null;
                    sendVirtualStickDataTask = null;
                }
                break;
            default:
                break;
        }
    }
    private class SendVirtualStickDataTask extends TimerTask {

        @Override
        public void run() {
            FlightController flightController = ModuleVerificationUtil.getFlightController();
            if (flightController == null) {
                return;
            }
            flightController.sendVirtualStickFlightControlData(new FlightControlData(pitch, roll, yaw, throttle),
                                new CommonCallbacks.CompletionCallback() {
                                    @Override
                                    public void onResult(DJIError djiError) {
                                    }
                                });
        }
    }

}
