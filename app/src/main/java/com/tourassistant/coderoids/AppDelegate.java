package com.tourassistant.coderoids;

import android.app.Application;

import com.google.android.gms.common.GoogleApiAvailability;
import com.tourassistant.coderoids.appdb.AppDatabase;
import com.tourassistant.coderoids.appdb.DatabaseClient;

public class AppDelegate extends Application {

    private boolean appRunning = false;
    private static AppDelegate instance = null;

    public static synchronized AppDelegate getInstance() {
        return instance;
    }

    public int isPlayServiceAvailable() {
        return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

}
