package com.dji.customsdk;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MapGUI extends RelativeLayout
implements View.OnTouchListener{

    private MainActivity mainContext;
    private View view;
    private VirtualSticks virtualSticks;

    public MapGUI(Context context) {
        super(context);
        this.mainContext = (MainActivity)context;
        view = mainContext.map;
        virtualSticks = mainContext.getVirtualSticks();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        System.out.println("A touch occured");
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            float x = event.getRawX();
            float y = event.getRawY();
            mainContext.showToast("x: " + x + "y: " + y);
            return true;
        }
        else {
            return false;
        }
    }
}
