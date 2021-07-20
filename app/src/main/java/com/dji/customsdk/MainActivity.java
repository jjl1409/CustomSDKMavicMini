package com.dji.customsdk;

import android.Manifest;
import android.app.Service;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.widget.Button;
import android.content.Context;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();
    private VirtualSticks virtualSticks;
    private CameraImaging cameraImaging;
    private Button btnSpin;
    private Button btnEnableVirtualStick;
    private Button btnDisableVirtualStick;
    private Button btnOrbit;
    private Button btnWaypoint;
    private SeekBar seekbarPitchVelocity;
    private SeekBar seekbarAngularVelocity;
    public TextView textPitchVelocity;
    public TextView textAngularVelocity;
    public TextView textLatitudeLongitude;
    public Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // When the compile and target version is higher than 22, please request the
        // following permissions at runtime to ensure the
        // SDK work well.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.VIBRATE,
                            Manifest.permission.INTERNET, Manifest.permission.ACCESS_WIFI_STATE,
                            Manifest.permission.WAKE_LOCK, Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.CHANGE_WIFI_STATE, Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
                            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.SYSTEM_ALERT_WINDOW,
                            Manifest.permission.READ_PHONE_STATE,
                    }
                    , 1);
        }
        handler = new Handler();
        setContentView(R.layout.activity_main);
        if(cameraImaging == null) {
            cameraImaging = new CameraImaging(this);
        }
        initUI(this);
        if(virtualSticks == null) {
            virtualSticks = new VirtualSticks(this);
        }
    }

    // Initializes all the UI elements other than the UXSDK elements
    protected void initUI(Context context) {
        btnEnableVirtualStick = (Button) findViewById(R.id.btn_enable_virtual_stick);
        btnDisableVirtualStick = (Button) findViewById(R.id.btn_disable_virtual_stick);
        btnSpin = (Button) findViewById(R.id.btn_spin);
        btnOrbit = (Button) findViewById(R.id.btn_orbit);
        btnWaypoint = (Button) findViewById(R.id.btn_waypoint);
        seekbarPitchVelocity = findViewById(R.id.seekbar_pitchvelocity);
        seekbarAngularVelocity = findViewById(R.id.seekbar_angularvelocity);
        textPitchVelocity = findViewById(R.id.text_pitchvelocity);
        textAngularVelocity = findViewById(R.id.text_angularvelocity);
        textLatitudeLongitude = findViewById(R.id.text_latitudelongitude);


        btnEnableVirtualStick.setOnClickListener(virtualSticks);
        btnDisableVirtualStick.setOnClickListener(virtualSticks);
        btnSpin.setOnClickListener(virtualSticks);
        btnOrbit.setOnClickListener(virtualSticks);
        btnWaypoint.setOnClickListener(virtualSticks);
        seekbarPitchVelocity.setOnSeekBarChangeListener(virtualSticks);
        seekbarAngularVelocity.setOnSeekBarChangeListener(virtualSticks);
        textPitchVelocity.setText("Pitch velocity: 0");
        textAngularVelocity.setText("Angular velocity: 0");
        textLatitudeLongitude.setText("Latitude: 0          Longitude: 0");
    }

    public void showToast(final String msg) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public CameraImaging getCamera() {
        return cameraImaging;
    }
}

