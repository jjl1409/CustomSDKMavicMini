package com.dji.customsdk;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.content.Context;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();
    private VirtualSticks virtualSticks;
    private CameraImaging cameraImaging;
    private TerrainFollowing terrainFollowing;
    private MapGUI mapGUI;
    private Button btnSpin;
    private Button btnDisableVirtualStick;
    private Button btnOrbit;
    private Button btnWaypoint;
    private Button btnMap;
    private Button btnReturn;
    private Button btnCamera;
    private SeekBar seekbarPitchVelocity;
    private SeekBar seekbarAngularVelocity;
    private ImageView imageMap;
    public ImageView drone;
    public ImageView target;
    public TextView textRollVelocity;
    public TextView textAngularVelocity;
    public TextView textLatitudeLongitude;
    public Handler handler;
    public View map;

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
//        setContentView(R.layout.sticks_virtual);
        if(cameraImaging == null) {
            cameraImaging = new CameraImaging(this);
        }
        if(virtualSticks == null) {
            virtualSticks = new VirtualSticks(this);
        }
        if (mapGUI == null){
            mapGUI = new MapGUI(this);
        }
        if(terrainFollowing == null) {
            InputStream inputStream = null;
            try {
                inputStream = getAssets().open("scan.csv");
                terrainFollowing = new TerrainFollowing(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        initMainUI(this);
    }

    // Initializes all the UI elements other than the UXSDK elements
    protected void initMainUI(Context context) {
        btnDisableVirtualStick = (Button) findViewById(R.id.btn_disable_virtual_stick);
        btnSpin = (Button) findViewById(R.id.btn_spin);
        btnOrbit = (Button) findViewById(R.id.btn_orbit);
        btnWaypoint = (Button) findViewById(R.id.btn_waypoint);
        btnMap = (Button) findViewById(R.id.btn_map);
        btnReturn = (Button) findViewById(R.id.btn_return);
        btnCamera = (Button) findViewById(R.id.btn_camera);
        seekbarPitchVelocity = findViewById(R.id.seekbar_rollvelocity);
        seekbarAngularVelocity = findViewById(R.id.seekbar_angularvelocity);
        textRollVelocity = findViewById(R.id.text_rollvelocity);
        textAngularVelocity = findViewById(R.id.text_angularvelocity);
        textLatitudeLongitude = findViewById(R.id.text_latitudelongitude);
        map = findViewById(R.id.map_gui);
        imageMap = findViewById(R.id.field);
        drone = findViewById(R.id.drone);
        target = findViewById(R.id.target);

        btnDisableVirtualStick.setOnClickListener(virtualSticks);
        btnSpin.setOnClickListener(virtualSticks);
        btnOrbit.setOnClickListener(virtualSticks);
        btnWaypoint.setOnClickListener(virtualSticks);
        btnReturn.setOnClickListener(virtualSticks);
        btnMap.setOnClickListener(mapGUI);
        btnReturn.setOnClickListener(mapGUI);
//        btnMap.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v){
//                map.setVisibility(View.VISIBLE);
//            }
//        });
//        btnReturn.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v){
//                map.setVisibility(View.GONE);
//            }
//        });
        imageMap.setOnTouchListener(mapGUI);
        seekbarPitchVelocity.setOnSeekBarChangeListener(virtualSticks);
        seekbarAngularVelocity.setOnSeekBarChangeListener(virtualSticks);
        textRollVelocity.setText("Roll velocity: 0");
        textAngularVelocity.setText("Angular velocity: 0");
        textLatitudeLongitude.setText("Latitude: 0                   Longitude: 0");
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

    public TerrainFollowing getTerrainFollowing() { return terrainFollowing; }

    public VirtualSticks getVirtualSticks() { return virtualSticks; }
}

