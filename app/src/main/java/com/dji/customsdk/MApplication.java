package com.dji.customsdk;

import android.app.Application;
import android.content.Context;

import com.secneo.sdk.Helper;

public class MApplication extends Application {

    private NewApplication newApplication;
    private VirtualSticks virtualSticks;
    @Override
    protected void attachBaseContext(Context paramContext) {
        super.attachBaseContext(paramContext);
        Helper.install(MApplication.this);
        if (newApplication == null) {
            newApplication = new NewApplication();
            newApplication.setContext(this);
        }
        if(virtualSticks == null) {
            virtualSticks = new VirtualSticks(this);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        newApplication.onCreate();
    }
}

