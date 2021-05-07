package com.tourassistant.coderoids.home;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.tourassistant.coderoids.R;
import com.tourassistant.coderoids.auth.LoginProcessActivity;
import com.tourassistant.coderoids.helpers.AppHelper;
import com.tourassistant.coderoids.models.FireBaseRegistration;
import com.tourassistant.coderoids.services.LocationService;
import com.tourassistant.coderoids.services.LocationThread;
import com.tourassistant.coderoids.starttrip.StartTrip;

import org.json.JSONArray;

import java.util.EventListener;
import java.util.List;

public class DashboardActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener {
    public static Activity instance;
    Toolbar toolbar;
    public DrawerLayout drawerLayout;
    public NavController navController;
    public NavigationView navigationView;
    ImageButton ibFriendRequest;
    View ActivityView;
    BottomNavigationView bottomNavigationView;
    TextView tvToolbarTitle;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public static final String PROPERTY_REG_ID = "registration_id";
    public BroadcastReceiver receiver;
    private DatabaseReference mDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResourceId());
        instance = this;
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setupNavigation();
        RegisterGCM();
        startLocationService();

//        EventListnerManager  eventListnerManager  = new EventListnerManager(this);
//        eventListnerManager.startListening();
//        eventListnerManager.initializeBroadCastRec(receiver);
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(AppHelper.currentProfileInstance!= null && AppHelper.currentProfileInstance.getUserId() != null) {
            mDatabase = FirebaseDatabase.getInstance().getReference();
            mDatabase.child("userChats").child(AppHelper.currentProfileInstance.getUserId()).child(AppHelper.currentProfileInstance.getUserId()).setValue("");
        }
//        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot snapshot) {
//                if (!snapshot.hasChild("userChats")) {
//                    mDatabase.setValue("userChats");
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });

    }

    protected int getLayoutResourceId() {
        return R.layout.activity_dashboard;
    }

    private void getRefreshedToken() {
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
                        String oldToken = prefs.getString("refreshedToken","");
                        if(oldToken.matches("") || !oldToken.matches(newToken)) {
                            editor.putString("refreshedToken", newToken);
                            editor.apply();

                        }

                        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
                        FireBaseRegistration fireBaseRegistration = new FireBaseRegistration();
                        fireBaseRegistration.setToken(newToken);
                        fireBaseRegistration.setTimeinMIllis(System.currentTimeMillis()+"");
                        rootRef.collection("RegistrationUserId").document(firebaseUser.getUid()).set(fireBaseRegistration);
                    }
                });
    }

    private void RegisterGCM() {
        if (checkPlayServices()) {
            getRefreshedToken();
        } else {
            Toast.makeText(this, "No valid Google Play Services APK found for FCM Registration", Toast.LENGTH_SHORT).show();
        }
    }

    public int isPlayServiceAvailable() {
        return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
    }

    private boolean checkPlayServices() {
        int resultCode = isPlayServiceAvailable();
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i("FCMRelated", "This device is not supported.");
            }
            return false;
        }
        return true;
    }

    private void setupNavigation() {
        toolbar = findViewById(R.id.dash_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        drawerLayout = findViewById(R.id.drawer_layout);
        tvToolbarTitle = findViewById(R.id.toolbar_title);
        navigationView = findViewById(R.id.nav_view);
        bottomNavigationView = findViewById(R.id.bttm_nav);
        ibFriendRequest = findViewById(R.id.friend_request);
        navController = Navigation.findNavController(this, R.id.dash_board_nav);
        NavigationUI.setupActionBarWithNavController(this, navController, drawerLayout);
        NavigationUI.setupWithNavController(navigationView, navController);
        NavigationUI.setupWithNavController(bottomNavigationView,
                navController);
        navigationView.setNavigationItemSelectedListener(this);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                item.setChecked(true);
                if (item.getItemId() == R.id.trips) {
                    navController.navigate(R.id.tripsFragment);
                } else if (item.getItemId() == R.id.requests) {
                    manageFriendRequest();
                } else {
                    navController.navigate(R.id.homeFragment);
                }
                return false;
            }
        });

        //BottomNavigationItemView  bottomNavigationItemView = bottomNavigationView.findViewById(R.id.trips);
        showBadge(this, bottomNavigationView, R.id.trips, "5");
        ibFriendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }

    public static void showBadge(Context context, BottomNavigationView
            bottomNavigationView, int itemId, String value) {
        removeBadge(bottomNavigationView, itemId);
        BottomNavigationItemView itemView = bottomNavigationView.findViewById(itemId);
        View badge = LayoutInflater.from(context).inflate(R.layout.layout_news_badge, bottomNavigationView, false);

        TextView tvTripCount = badge.findViewById(R.id.badge_text_view);
        tvTripCount.setText(value);
        itemView.addView(badge);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
                FirebaseUser users = FirebaseAuth.getInstance().getCurrentUser();
                String uid = users.getUid();
                rootRef.collection("Trips").document(uid).collection("UserTrips").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<DocumentSnapshot> doc = task.getResult().getDocuments();
                            tvTripCount.setText(doc.size() + "");
                        }
                    }
                });
            }
        });


    }

    public static void removeBadge(BottomNavigationView bottomNavigationView, int itemId) {
        BottomNavigationItemView itemView = bottomNavigationView.findViewById(itemId);
        if (itemView.getChildCount() == 3) {
            itemView.removeViewAt(2);
        }
    }

    private void manageFriendRequest() {
        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        FirebaseUser users = FirebaseAuth.getInstance().getCurrentUser();
        String uid = users.getUid();
        rootRef.collection("Users").document(uid).collection("FriendRequestsReceived").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isComplete()) {
                    List<DocumentSnapshot> friendRequest = task.getResult().getDocuments();
                    if (friendRequest.size() > 0) {
                        AppHelper.friendRequests = friendRequest;
                        navController.navigate(R.id.friendRequestFragment);
                    } else
                        Toast.makeText(DashboardActivity.this, "No New Requests", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    @Override
    public boolean onSupportNavigateUp() {
        tvToolbarTitle.setText("Tour Assistant");
        bottomNavigationView.setVisibility(View.VISIBLE);
        return NavigationUI.navigateUp(Navigation.findNavController(this, R.id.dash_board_nav), drawerLayout);
    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        menuItem.setChecked(true);
        drawerLayout.closeDrawers();
        int id = menuItem.getItemId();

        switch (id) {
            case R.id.main_fragment:
                bottomNavigationView.setVisibility(View.VISIBLE);
                navController.navigate(R.id.homeFragment);
                break;
            case R.id.settings_fragment:
                Toast.makeText(this, "In Progress", Toast.LENGTH_SHORT).show();
                //bottomNavigationView.setVisibility(View.GONE);
                //navController.navigate(R.id.settings_fragment);
                break;
            case R.id.profile:
                tvToolbarTitle.setText("");
                bottomNavigationView.setVisibility(View.GONE);
                navController.navigate(R.id.profileFragment);
                break;
            case R.id.logout:
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                mAuth.signOut();
                AppHelper.interestUser = new JSONArray();
                Intent intent = new Intent(this, LoginProcessActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
        }
        return true;

    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(receiver != null)
            unregisterReceiver(receiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.registerReceiver(receiver, new IntentFilter("com.coderoids.notification"));
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

    public void startLocationService() {
        boolean isLocationServiceRunning = isServiceRunning(LocationService.class);
        if (isLocationServiceRunning ) {
            stopLocationService();
        }
        new Handler().postDelayed(new Runnable() {
            public void run() {
                try {
                    LocationThread locationThread = new LocationThread(DashboardActivity.this,"LocationService");
                    locationThread.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 3000);
        LocationService.shouldContinueRunnable = true;
    }

    public void stopLocationService() {
        boolean isLocationServiceRunning = isServiceRunning(LocationService.class);
        if (isLocationServiceRunning) {
            stopService(new Intent(this, LocationService.class));
            if(LocationThread.t != null){
                LocationThread.t.interrupt();
                LocationThread.t = null;
            }
        }
    }

    public void initializeBroadCastRec(BroadcastReceiver receiver) {

    }
}