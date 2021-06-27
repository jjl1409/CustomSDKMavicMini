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

    private Button btnEnableVirtualStick;
    private Button btnDisableVirtualStick;

    private TextView textView;

    private Timer sendVirtualStickDataTimer;
//    private SendVirtualStickDataTask sendVirtualStickDataTask;
    private Context context;
    private float pitch;
    private float roll;
    private float yaw;
    private float throttle;

    public VirtualSticks(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
    }

    @Override
    public void onClick(View v) {
        System.out.println("Help");
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
                        Toast.makeText(context,"Failed to activate virtual sticks",Toast.LENGTH_SHORT).show();
                    }
                });
                flightController.setVirtualStickAdvancedModeEnabled(false);
                Toast.makeText(context,"Activated virtual sticks",Toast.LENGTH_SHORT).show();
                break;

            case R.id.btn_disable_virtual_stick:
                flightController.setVirtualStickModeEnabled(false, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        DialogUtils.showDialogBasedOnError(context, djiError);
                        Toast.makeText(context,"Failed to deactivate virtual sticks",Toast.LENGTH_SHORT).show();
                    }
                });
                flightController.setVirtualStickAdvancedModeEnabled(false);
                Toast.makeText(context,"Deactivated virtual sticks",Toast.LENGTH_SHORT).show();
                break;

            default:
                break;
        }
    }



}
