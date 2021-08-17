package com.dji.customsdk;

import android.content.Context;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MapGUI extends RelativeLayout{
    private Context context;
    public MapGUI(Context context) {
        super(context);
        this.context = context;
    }
}
