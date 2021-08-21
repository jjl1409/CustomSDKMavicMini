package com.dji.customsdk;

import android.content.Context;
import android.widget.Toast;

import com.dji.customsdk.utils.DialogUtils;
import com.dji.customsdk.utils.ModuleVerificationUtil;

import androidx.annotation.NonNull;

import java.util.TimerTask;
import java.util.Timer;

import dji.common.camera.SettingsDefinitions;
import dji.common.error.DJIError;
import dji.common.util.CommonCallbacks;
import dji.sdk.camera.Camera;
import dji.sdk.media.MediaFile;
import dji.sdk.products.Aircraft;

public class CameraImaging {

    private MainActivity context;
    private Camera camera;
    private Timer cameraTimer;
    private CameraTask cameraTask;

    public CameraImaging(Context context) {
        this.context = (MainActivity)context;
        this.init();
    }

    /**
     * Every commands relative to the shooting photos are only allowed executed in shootphoto work
     * mode.
     */
    private void init() {
        if (ModuleVerificationUtil.isCameraModuleAvailable()) {
            camera = NewApplication.getAircraftInstance().getCamera();
            camera.setMode(SettingsDefinitions.CameraMode.SHOOT_PHOTO, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    DialogUtils.showDialogBasedOnError(context, djiError);
                }
            });
            camera.setMediaFileCallback(new MediaFile.Callback() {
                @Override
                public void onNewFile(@NonNull MediaFile mediaFile) {
                    Toast.makeText(context,"Took a picture",Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public boolean isCameraAvailable(){
        Aircraft aircraft = NewApplication.getAircraftInstance();
        return (aircraft  != null && aircraft.getCamera() != null);
    }
    protected void takePhoto() {
        if (!isCameraAvailable()){
            return;
        }
        camera = NewApplication.getAircraftInstance().getCamera();
            if (camera != null) {
            System.out.println("Trying to take photo right now!");
            SettingsDefinitions.ShootPhotoMode photoMode = SettingsDefinitions.ShootPhotoMode.SINGLE; // Set the camera capture mode as Single mode
            camera.setShootPhotoMode(photoMode, new CommonCallbacks.CompletionCallback(){
                @Override
                public void onResult(DJIError djiError) {
                    if (null == djiError) {
                        context.handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                camera.startShootPhoto(new CommonCallbacks.CompletionCallback() {
                                    @Override
                                    public void onResult(DJIError djiError) {
                                        if (djiError == null) {
                                            context.showToast("take photo: success");
                                        } else {
                                            context.showToast(djiError.getDescription());
                                        }
                                    }
                                });
                            }
                        }, 2000);
                    }
                }
            });
        }
    }

    public void startCameraTimer(int initialDelay, int repeatedDelay) {
        stopCameraTimer();
        if (null == cameraTimer) {
            cameraTask = new CameraImaging.CameraTask();
            cameraTimer = new Timer();
            cameraTimer.schedule(cameraTask, initialDelay, repeatedDelay);
        }
    }

    public void stopCameraTimer() {
        if (cameraTimer != null){
            cameraTimer.cancel();
            cameraTimer = null;
            cameraTask = null;
        }
    }
    private class CameraTask extends TimerTask {

        @Override
        public void run() {
            takePhoto();
        }
    }
}
