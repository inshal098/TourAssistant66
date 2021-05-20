package com.tourassistant.coderoids.home;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.tourassistant.coderoids.BaseActivity;
import com.tourassistant.coderoids.R;
import com.tourassistant.coderoids.auth.LoginProcessActivity;
import com.tourassistant.coderoids.helpers.AppHelper;
import com.tourassistant.coderoids.models.FireBaseRegistration;
import com.tourassistant.coderoids.services.LocationService;
import com.tourassistant.coderoids.services.LocationThread;
import com.tourassistant.coderoids.starttrip.StartTrip;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;

public class DashboardActivity extends BaseActivity {
    public static DashboardActivity instance;
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
    GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResourceId());
        instance = this;
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setupNavigation();
        RegisterGCM();
        startLocationService();
        localEventListener();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    public void updateNavigation() {
        setupNavigation();
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_dashboard;
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
                Toast.makeText(instance, "Google Services Not Supported", Toast.LENGTH_SHORT).show();
                Log.i("FCMRelated", "This device is not supported.");
            }
            return false;
        } else
            Toast.makeText(instance, "Google Services Not Supported", Toast.LENGTH_SHORT).show();
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
                    navController.navigate(R.id.friendRequestFragment);
                    //manageFriendRequest();
                } else if (item.getItemId() == R.id.news_feed) {
                    navController.navigate(R.id.news_feed);
                } else {
                    navController.navigate(R.id.homeFragment);
                }
                return false;
            }
        });

        //BottomNavigationItemView  bottomNavigationItemView = bottomNavigationView.findViewById(R.id.trips);
        showBadge(this, bottomNavigationView, R.id.trips, "0");
        ibFriendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }


    private void localEventListener() {
        rootRef.collection("Trips").document(firebaseUser.getUid()).collection("UserTrips").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                try {
                    if (!value.isEmpty()) {
                        if (DashboardActivity.instance != null) {
                            setupNavigation();
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    FirebaseCrashlytics.getInstance().recordException(ex);
                }

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
        getUsersTrips(tvTripCount);
    }

    public static void getUsersTrips(TextView textView) {
        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        FirebaseUser users = FirebaseAuth.getInstance().getCurrentUser();
        String uid = users.getUid();
        rootRef.collection("Trips").document(uid).collection("UserTrips").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<DocumentSnapshot> doc = task.getResult().getDocuments();
                    textView.setText(doc.size() + "");
                }
            }
        });
    }

    public static void removeBadge(BottomNavigationView bottomNavigationView, int itemId) {
        BottomNavigationItemView itemView = bottomNavigationView.findViewById(itemId);
        if (itemView.getChildCount() == 3) {
            itemView.removeViewAt(2);
        }
    }

    public void manageFriendRequest() {
        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        FirebaseUser users = FirebaseAuth.getInstance().getCurrentUser();
        String uid = users.getUid();
        rootRef.collection("Users").document(uid).collection("FriendRequestsReceived").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                try {
                    if (task.isComplete()) {
                        List<DocumentSnapshot> friendRequest = task.getResult().getDocuments();
                        if (friendRequest.size() > 0) {
                            AppHelper.friendRequests = friendRequest;
                            navController.navigate(R.id.friendRequestFragment);
                        } else
                            Toast.makeText(DashboardActivity.this, "No New Requests", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    FirebaseCrashlytics.getInstance().recordException(ex);
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
                FirebaseMessaging.getInstance().deleteToken();
                AppHelper.interestUser = new JSONArray();
                mGoogleSignInClient.signOut();
                Intent intent = new Intent(DashboardActivity.this, LoginProcessActivity.class);
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
    public void RequestFinished(String fragmentName, String apiName, String responseString) {
        Toast.makeText(this, responseString, Toast.LENGTH_LONG).show();
    }

    @Override
    public void RequestSecureFinished(String fragmentName, String apiName, String responseString) {
        Toast.makeText(this, responseString, Toast.LENGTH_LONG).show();

    }
}