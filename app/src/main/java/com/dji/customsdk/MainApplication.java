package com.dji.customsdk;

import android.app.Application;
import android.content.Context;

import com.secneo.sdk.Helper;

public class MainApplication extends Application {

    private NewApplication newApplication;
    @Override
    protected void attachBaseContext(Context paramContext) {
        super.attachBaseContext(paramContext);
        Helper.install(MainApplication.this);
        if (newApplication == null) {
            newApplication = new NewApplication();
            newApplication.setContext(this);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        newApplication.onCreate();
    }
}