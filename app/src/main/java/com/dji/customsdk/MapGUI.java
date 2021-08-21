package com.dji.customsdk;

import android.content.Context;
import android.content.res.Resources;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MapGUI extends RelativeLayout
implements View.OnTouchListener, View.OnClickListener{

    private MainActivity mainContext;
    private View view;
    private VirtualSticks virtualSticks;
    float windowWidth= 1280;
    float windowHeight = 736;
    float actualHeight = 601;
    float actualWidth = 962;
    float density;

    public MapGUI(Context context) {
        super(context);
        this.mainContext = (MainActivity)context;
        view = mainContext.map;
        virtualSticks = mainContext.getVirtualSticks();
        density = getResources().getDisplayMetrics().density;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_map:
                mainContext.map.setVisibility(View.VISIBLE);
                break;
            case R.id.btn_return:
                mainContext.map.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        System.out.println("A touch occured");
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            float x = event.getRawX();
            float y = event.getRawY();
            float offsetX = -12.5f * density;
//            float offsetY = 36.5f * density;
            float offsetY = -37.5f * density;
//            float y = Resources.getSystem().getDisplayMetrics().widthPixels;
//            float x = Resources.getSystem().getDisplayMetrics().heightPixels;
            mainContext.showToast("x: " + x + " y: " + y);
            x = (x + offsetX);
            y = (y + offsetY);
            mainContext.target.setVisibility(View.VISIBLE);
            mainContext.target.setX(x);
            mainContext.target.setY(y);
            return true;
        }
        else {
            return false;
        }
    }
}
