package com.dji.customsdk;

import android.content.Context;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MapGUI extends RelativeLayout
    implements View.OnClickListener {

    private Context context;
    private View view;

    public MapGUI(Context context) {
        super(context);
        this.context = context;
        view = ((MainActivity)context).map;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_return:
                view.setVisibility(View.GONE);
                break;
            case R.id.btn_map:
                view.setVisibility(View.VISIBLE);
                break;
        }
    }
}
