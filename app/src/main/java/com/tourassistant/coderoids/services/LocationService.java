package com.tourassistant.coderoids.services;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tourassistant.coderoids.R;
import com.tourassistant.coderoids.helpers.AppHelper;
import com.tourassistant.coderoids.helpers.LocationHelper;
import com.tourassistant.coderoids.models.NotificationPublish;
import com.tourassistant.coderoids.models.TripCurrentLocation;


import java.util.ArrayList;
import java.util.Random;

public class LocationService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    public static LocationService instance;
    private static final String TAG = "LocationService";
    public static boolean shouldContinueRunnable = true;
    ArrayList<TripCurrentLocation> arrayTruckLocation;
    public static Runnable runnableupdateTruck;
    int frequency = 200000;
    public static AsyncTask uLocation;

    LocationRequest locationRequest;
    private GoogleApiClient googleApiClient;
    private IBinder mBinder = new LocationServiceBinder();
    SharedPreferences.Editor editorLogin;
    SharedPreferences prefLogin;
    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("Service", " Location");
    }

    @Override

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("Service", " Location");
        shouldContinueRunnable = true;
        instance = this;
        prefLogin = getSharedPreferences("logindata", Context.MODE_PRIVATE);
        editorLogin = prefLogin.edit();
        editorLogin.apply();
        startTracking();
        arrayTruckLocation = new ArrayList<>();
        updateTruck();
        //startListening();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startMyOwnForeground();
        }
        return START_STICKY;
    }

    public void startListening() {
        try {
            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
            FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
            mDatabase.child("NotificationPool");
            ValueEventListener postListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    manageAndPublishNotification(dataSnapshot);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    DatabaseError databaseError = error;
                }
            };
            mDatabase.addValueEventListener(postListener);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void manageAndPublishNotification(DataSnapshot dataSnapshot) {
        for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
            NotificationPublish value = postSnapshot.getValue(NotificationPublish.class);
            Intent intent = new Intent("com.coderoids.notification");
            intent.putExtra("message", value.getNotificationMessage());
            intent.putExtra("type", value.getNotificationType());
            intent.putExtra("notif_id", value.getNotificationTime());
            instance.sendBroadcast(intent);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startMyOwnForeground() {
        try {
            String NOTIFICATION_CHANNEL_ID = "location_service";
            String channelName = "location_service";
            NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH);
            chan.setLightColor(Color.BLUE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(chan);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
            Notification notification = notificationBuilder.setOngoing(true)
                    .setSmallIcon(R.drawable.ic_cloudy)
                    .setContentTitle("App is running in background")
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .setOngoing(true)
                    .build();
            startForeground(START_REDELIVER_INTENT, notification);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static int randInt(int min, int max) {

        // NOTE: This will (intentionally) not run as written so that folks
        Random rand = new Random();

        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }

    void updateTruck() {
        runnableupdateTruck = new Runnable() {
            @Override
            public void run() {
                if (shouldContinueRunnable) {
                    try {
                        LocationHelper location = LocationHelper.getInstance();
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                                ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        double latitude = Double.parseDouble(location.getLatitude());
                        double longitude = Double.parseDouble(location.getLongitude());
                        Log.e("Service", " Lat" +  latitude +"," +longitude);
                        if(prefLogin.getString("isTripInProgress" ,"0").matches("1")) {
                            String currentTime = String.valueOf(System.currentTimeMillis());
                            TripCurrentLocation tripCurrentLocation = new TripCurrentLocation();
                            tripCurrentLocation.setLatitude(latitude + "");
                            tripCurrentLocation.setLongitude(longitude + "");
                            tripCurrentLocation.setSpeed(location.getSpeed() + "");
                            tripCurrentLocation.setTime(currentTime + "");
                            //FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
                            //rootRef.collection("PublicTrips").document(AppHelper.tripEntityList.getFirebaseId()).collection("TripCoordinates").document(currentTime).set(tripCurrentLocation);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
//            }
        };
        uLocation = new updateLocation();
        AppHelper.runAsyncTaskWithout(uLocation);
    }

    public int isPlayServiceAvailable() {
        return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
    }

    private void startTracking() {
        if (isPlayServiceAvailable() == ConnectionResult.SUCCESS) {

            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addApiIfAvailable(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

            if (!googleApiClient.isConnected() || !googleApiClient.isConnecting()) {
                googleApiClient.connect();
            }
        } else {
            Log.e(TAG, "unable to connect to google play services.");
        }
    }

    public class updateLocation extends AsyncTask<Object, String, String>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
        }

        @Override

        protected String doInBackground(Object... params) {
                try {
                    runnableupdateTruck.run();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            try {
                Thread.sleep(frequency);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
                Thread.currentThread().interrupt();
                /*throw new RuntimeException(ex);*/
            }
            return "";
        }

        @Override
        protected void onPostExecute(String s)
        {
            super.onPostExecute(s);
            if (shouldContinueRunnable) {
                uLocation = new updateLocation();
                AppHelper.runAsyncTaskWithout(uLocation);
            }
        }
    }

    public  static boolean checkUpdateLocationAsyncRunning(){
        if (uLocation != null ) {
            if (uLocation.getStatus() != AsyncTask.Status.RUNNING ) {
                uLocation.cancel(true);
                AppHelper.runAsyncTaskWithout(uLocation);
            }
            return true;
        }
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            shouldContinueRunnable = false;
            stopLocationUpdates();
            Runnable myRunnableThread = new timeRunner();
            Thread myThread = new Thread(myRunnableThread);
            myThread.start();

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
                stopSelf();
            Log.v("Rezsponse", "Destroy Location Data");
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public class timeRunner implements Runnable {
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(60000);
                    stopForeground(true);
                    stopSelf();
                    Thread.currentThread().interrupt();

                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (Exception ignored) {

                }
            }
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override

    public void onLocationChanged(Location location) {
        if (location != null) {

            if (location.getAccuracy() < 500.0f) {
            }
            LocationHelper helper = LocationHelper.getInstance();
            helper.setLatitude("" + location.getLatitude());
            helper.setLongitude("" + location.getLongitude());
            helper.setSpeed("" + 3.6 * location.getSpeed()); //changed from 4
            helper.setBearing("" + location.getBearing());
            helper.setGpsCoordinate( location.getLatitude() + "," + location.getLongitude());
            helper.setLocation(location);
        }
    }

    public void stopLocationUpdates() {
        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
        if (uLocation != null) {
            uLocation.cancel(true);
        }
    }


    public static void startBreakChecking(){
//        executor = Executors.newScheduledThreadPool(1);
//
//
//        runnable = new Runnable() {
//            @Override
//            public void run() {
//                LocationHelper helper = LocationHelper.getInstance();
//                if (preLat.matches("")){
//                    preLat = helper.getLatitude();
//                    preLong = helper.getLongitude();
//                }
//                else{
//                    Location loc1 = new Location("");
//                    loc1.setLatitude(Double.parseDouble(preLat));
//                    loc1.setLongitude(Double.parseDouble(preLong));
//
//                    Location loc2 = new Location("");
//                    loc2.setLatitude(Double.parseDouble(helper.getLatitude()));
//                    loc2.setLongitude(Double.parseDouble(helper.getLongitude()));
//
//                    float distanceInMeters = loc1.distanceTo(loc2);
//                    if (distanceInMeters>10.0f){
//                        BaseActivity.instance.reportBreakBreach();
//
//                    }
//                }
//            }
//        };
//        future = executor.scheduleWithFixedDelay(runnable, 0, 60, TimeUnit.SECONDS);
    }

    public static void stopBreakChecking(){
//        future.cancel(true);
//        future = null;
//        executor = null;
//        preLong = "";
//        preLat = "";
    }

    /**
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
    @Override

    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected");

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000); // milliseconds
        locationRequest.setFastestInterval(1000); // the fastest rate in milliseconds at which your app can handle location updates
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient, locationRequest, this);
    }

    @Override

    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed");

        stopLocationUpdates();
        stopSelf();
    }

    @Override

    public void onConnectionSuspended(int i) {
        Log.e(TAG, "GoogleApiClient connection has been suspend");
    }

    public class LocationServiceBinder extends Binder {
        LocationService getService() {
            return LocationService.this;
        }
    }



}

