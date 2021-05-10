package com.tourassistant.coderoids;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.tourassistant.coderoids.helpers.AppHelper;
import com.tourassistant.coderoids.home.DashboardActivity;
import com.tourassistant.coderoids.home.fragments.HomeFragment;
import com.tourassistant.coderoids.home.fragments.TripRoomFragment;
import com.tourassistant.coderoids.interfaces.RequestInterface;
import com.tourassistant.coderoids.models.FireBaseRegistration;
import com.tourassistant.coderoids.services.LocationService;
import com.tourassistant.coderoids.services.LocationThread;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public abstract class BaseActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener, RequestInterface {
    public static BaseActivity baseActivityInstance;
    public DatabaseReference mDatabase;
    public BroadcastReceiver receiver;
    public FirebaseFirestore rootRef;
    public FirebaseUser firebaseUser;
    public SharedPreferences prefLogin;
    public SharedPreferences.Editor editorLogin;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        baseActivityInstance = this;
        rootRef = FirebaseFirestore.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        prefLogin = getSharedPreferences("logindata", Context.MODE_PRIVATE);
        editorLogin = prefLogin.edit();
        editorLogin.apply();
        if (AppHelper.currentProfileInstance != null && AppHelper.currentProfileInstance.getUserId() != null) {
            mDatabase = FirebaseDatabase.getInstance().getReference();
            mDatabase.child("userChats").child(AppHelper.currentProfileInstance.getUserId()).child(AppHelper.currentProfileInstance.getUserId()).setValue("");
        }
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    if (intent != null) {
                        JSONObject jsonArray = new JSONObject(intent.getStringExtra("message"));
                        JSONArray messagObj = new JSONArray(jsonArray.getString("notificationMessage"));
                        JSONObject jsonObject = messagObj.getJSONObject(0);

                        if (intent.getStringExtra("notificationType").contains("PublicTripsRequest")) {
                            if (HomeFragment.instance != null) {
                                HomeFragment.instance.getAllTrips();
                                Toast.makeText(context, "Go To Trip Listing To Approve Join Request", Toast.LENGTH_SHORT).show();
                            }
                        } else if (intent.getStringExtra("notificationType").contains("FollowRequest")) {
                            DashboardActivity.instance.manageFriendRequest();
                        } else if (intent.getStringExtra("notificationType").contains("FollowRequestAccept")) {
                            Toast.makeText(context, "Your Follow Request is Accepted", Toast.LENGTH_SHORT).show();
                        } else if (intent.getStringExtra("notificationType").contains("TripInProgress")) {
                            String tripId = jsonObject.getString("id");
                            AppHelper.inProgressTripId = tripId;
                            Toast.makeText(context, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                            if(TripRoomFragment.instance != null){
                                TripRoomFragment.instance.updateTripUI(AppHelper.inProgressTripId);
                            }
                        }
                    }
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }

            }
        };
    }


    protected abstract int getLayoutResourceId();


    public void getRefreshedToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w("Status", "Fetching FCM registration token failed", task.getException());
                            return;
                        }
                        // Get new FCM registration token
                        final SharedPreferences prefs = getSharedPreferences("FCMData", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();

                        String newToken = task.getResult();
                        String oldToken = prefs.getString("refreshedToken", "");
                        if (oldToken.matches("") || !oldToken.matches(newToken)) {
                            editor.putString("refreshedToken", newToken);
                            editor.apply();

                        }
                        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
                        FireBaseRegistration fireBaseRegistration = new FireBaseRegistration();
                        fireBaseRegistration.setToken(newToken);
                        fireBaseRegistration.setTimeinMIllis(System.currentTimeMillis() + "");
                        rootRef.collection("RegistrationUserId").document(firebaseUser.getUid()).set(fireBaseRegistration);
                    }
                });
    }

    public void startLocationService() {
        boolean isLocationServiceRunning = isServiceRunning(LocationService.class);
        if (isLocationServiceRunning) {
            stopLocationService();
        }
        new Handler().postDelayed(new Runnable() {
            public void run() {
                try {
                    LocationThread locationThread = new LocationThread(BaseActivity.this, "LocationService");
                    locationThread.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 3000);
        LocationService.shouldContinueRunnable = true;
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        assert manager != null;
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
            stopService(new Intent(this, LocationService.class));
            if (LocationThread.t != null) {
                LocationThread.t.interrupt();
                LocationThread.t = null;
            }
        }
    }

    @Override
    public void onBackPressed() {

    }

    public void initializeBroadCastRec(BroadcastReceiver receiver) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (receiver != null)
            unregisterReceiver(receiver);
    }

    @Override
    protected void onResume() {
        this.registerReceiver(receiver, new IntentFilter("com.coderoids.notification"));
        super.onResume();
    }


}
