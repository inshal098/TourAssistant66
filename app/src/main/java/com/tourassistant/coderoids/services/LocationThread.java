package com.tourassistant.coderoids.services;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import static com.tourassistant.coderoids.services.LocationService.checkUpdateLocationAsyncRunning;

public class LocationThread extends Thread {

    public static Thread t;
    private String threadName;
    private Context context;
    private int locationCount = 0;
    private SharedPreferences loginPreferences;

    public LocationThread( Context mContext, String threadName) {
        this.context = mContext;
        this.threadName = threadName;
        loginPreferences = context.getSharedPreferences("logindata", Context.MODE_PRIVATE);
    }

    public void run() {
        try {
            try {
                startLocationService();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void start () {
        if (t == null ) {
//            if (!t.isAlive()) {
            t = new Thread(this, threadName);
            t.start();
//            }
        }
    }

    public void startLocationService() throws InterruptedException {

        while (true) {
            if (locationCount < 8) {
                boolean isLocationServiceRunning = isServiceRunning(LocationService.class);
                if (!isLocationServiceRunning) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        context.startForegroundService(new Intent(context, LocationService.class));
                    else
                        context.startService(new Intent(context, LocationService.class));
                    SharedPreferences.Editor editorLogin = loginPreferences.edit();
                    editorLogin.putBoolean("isLocationServiceRunning", true).apply();
                    LocationService.shouldContinueRunnable = true;
                    break;
                } else if (!checkUpdateLocationAsyncRunning()) {
                    stopLocationService();
                    locationCount++;
                    Thread.sleep(2000);
                }
            }
        }
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void stopLocationService() {
        boolean isLocationServiceRunning = isServiceRunning(LocationService.class);
        if (isLocationServiceRunning) {
            context.stopService(new Intent(context, LocationService.class));
            if(LocationThread.t != null){
                LocationThread.t.interrupt();
                LocationThread.t = null;
            }
            SharedPreferences.Editor editor = loginPreferences.edit();
            editor.putBoolean("isLocationServiceRunning", false).apply();
        }
    }

    public void interrupt(){
        stopLocationService();
        t.interrupt();
        t= null;
    }
}

