package com.tourassistant;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.tourassistant.coderoids.R;
import com.tourassistant.coderoids.helpers.AppHelper;
import com.tourassistant.coderoids.helpers.PermissionHelper;
import com.tourassistant.coderoids.helpers.RuntimePermissionsActivity;
import com.tourassistant.coderoids.home.DashboardActivity;
import com.tourassistant.coderoids.home.fragments.FilterPublicTrips;
import com.tourassistant.coderoids.interfaces.LoginHelperInterface;
import com.tourassistant.coderoids.interfaces.RequestCompletionListener;
import com.tourassistant.coderoids.models.Profile;
import com.tourassistant.coderoids.services.LocationService;
import com.tourassistant.coderoids.services.LocationThread;
import com.tourassistant.coderoids.starttrip.StartTrip;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class PreDashBoardActivity extends RuntimePermissionsActivity implements RequestCompletionListener, LoginHelperInterface {
    ProgressDialog progressDialog;
    RequestCompletionListener requestCompletionListener;
    FirebaseFirestore rootRef;
    FirebaseUser users;
    Button error;
    PermissionHelper loginProcessHelper;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_dash_board);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        rootRef = FirebaseFirestore.getInstance();
        users = FirebaseAuth.getInstance().getCurrentUser();
        loginProcessHelper = new PermissionHelper(PreDashBoardActivity.this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please Wait While We Load your Content...");
        progressDialog.setIndeterminate(true);
        progressDialog.show();
        error = findViewById(R.id.error);
        requestCompletionListener = (RequestCompletionListener) this;
        error.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleState();
            }
        });
        handleState();
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
        if (isLocationServiceRunning) {
            stopLocationService();
        }
        new Handler().postDelayed(new Runnable() {
            public void run() {
                try {
                    LocationThread locationThread = new LocationThread(PreDashBoardActivity.this, "LocationService");
                    locationThread.start();
                } catch (Exception e) {
                    e.printStackTrace();
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
            }
        }, 3000);
        LocationService.shouldContinueRunnable = true;
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
    public void onPermissionsGranted(int requestCode) {
        requestCurrentLocation();
    }

    private void handleState() {
        if (AppHelper.isNetworkAvailable(getApplicationContext()))
            manageUserPreferences();
        else {
            progressDialog.dismiss();
            error.setText("No Internet Available, Please Connect And Retry");
        }
    }

    private void manageUserPreferences() {
        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        rootRef.collection("Users").document(users.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                try {
                    AppHelper.currentProfileInstance = documentSnapshot.toObject(Profile.class);
                    if (AppHelper.currentProfileInstance != null)
                        AppHelper.currentProfileInstance.setUserId(documentSnapshot.getId());
                    if (AppHelper.currentProfileInstance.getInterests() != null && !AppHelper.currentProfileInstance.getInterests().matches(""))
                        AppHelper.interestUser = new JSONArray(AppHelper.currentProfileInstance.getInterests());
                } catch (Exception ex) {
                    ex.printStackTrace();
                    FirebaseCrashlytics.getInstance().recordException(ex);
                }
            }
        });

        rootRef.collection("PublicTrips").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                try {
                    if (task.isComplete()) {
                        List<DocumentSnapshot> publicTrips = task.getResult().getDocuments();
                        if (publicTrips.size() > 0) {
                            FilterPublicTrips filterPublicTrips = new FilterPublicTrips(PreDashBoardActivity.this, publicTrips, requestCompletionListener);
                            AppHelper.filteredTrips = new ArrayList<>();
                            filterPublicTrips.filteredTrips();
                        } else
                            requestCompletionListener.onListFilteredCompletion(false);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    FirebaseCrashlytics.getInstance().recordException(ex);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                requestCompletionListener.onListFilteredCompletion(false);
                FirebaseCrashlytics.getInstance().recordException(e);
            }
        });
    }

    @Override
    public void onListFilteredCompletion(boolean status) {
        manageFriends();
    }

    private void manageFriends() {
        rootRef.collection("Users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                try {
                    if (task.isComplete()) {
                        List<DocumentSnapshot> allUsers = task.getResult().getDocuments();
                        FirebaseUser users = FirebaseAuth.getInstance().getCurrentUser();
                        AppHelper.allUsers = new ArrayList<>();
                        for (int i = 0; i < allUsers.size(); i++) {
                            DocumentSnapshot documentSnapshot = allUsers.get(i);
                            if (!users.getUid().matches(documentSnapshot.getId())) {
                                AppHelper.allUsers.add(documentSnapshot);
                            }
                        }
                        requestCompletionListener.onAllUsersCompletion(true);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    FirebaseCrashlytics.getInstance().recordException(ex);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
                FirebaseCrashlytics.getInstance().recordException(e);
            }
        });
    }

    @Override
    public void onAllUsersCompletion(boolean status) {
        try {
            if (status) {
                requestCompletionListener.onAllUsersCompletion(false);
            } else {
                if (AppHelper.currentProfileInstance != null) {
                    rootRef.collection("Users").document(users.getUid()).collection("Friends").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            try {
                                if (task.isComplete()) {
                                    List<DocumentSnapshot> friendsList = task.getResult().getDocuments();
                                    AppHelper.allFriends = friendsList;
                                    DocumentReference uidRef4 = rootRef.collection("Users").document(AppHelper.currentProfileInstance.getUserId());
                                    uidRef4.update("followers", AppHelper.allFriends.size() + "");
                                    uidRef4.update("following", AppHelper.allFriends.size() + "");
                                    progressDialog.dismiss();
                                    if (!loginProcessHelper.checkGpsStatus()) {
                                        loginProcessHelper.askGPSPermission();
                                    } else if (!checkPermissions())
                                        PreDashBoardActivity.super.requestAppPermissions(loginProcessHelper.permissionsManager(), R.string.runtime_permissions_txt
                                                , loginProcessHelper.REQUEST_PERMISSIONS);
                                    else
                                        requestCurrentLocation();
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                FirebaseCrashlytics.getInstance().recordException(ex);
                            }
                        }
                    });
                } else {
                    progressDialog.dismiss();
                    if (!checkPermissions())
                        PreDashBoardActivity.super.requestAppPermissions(loginProcessHelper.permissionsManager(), R.string.runtime_permissions_txt
                                , loginProcessHelper.REQUEST_PERMISSIONS);
                    else
                        requestCurrentLocation();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            FirebaseCrashlytics.getInstance().recordException(ex);
        }
    }

    @Override
    public void onPrivacyPolicy(String state) {
        if (!checkPermissions())
            PreDashBoardActivity.super.requestAppPermissions(loginProcessHelper.permissionsManager(), R.string.runtime_permissions_txt
                    , loginProcessHelper.REQUEST_PERMISSIONS);
        else
            requestCurrentLocation();
    }


    private void requestCurrentLocation() {
        try {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED) {
                startLocationService();
                startActivity(new Intent(PreDashBoardActivity.this, DashboardActivity.class));
                finish();
            } else {
                // TODO: Request fine location permission
                checkLocationPermission();
                Log.d("Permission", "Request fine location permission.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            FirebaseCrashlytics.getInstance().recordException(ex);
        }
    }


    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission")
                        .setMessage("Please Provide Location Permission")
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(PreDashBoardActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    requestCurrentLocation();
                } else {
                }
                return;
            }
        }
    }


    private boolean checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            int permissionLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION);
            if (permissionLocation < 0) {
                return false;
            } else {
                int permissionLocation1 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
                int permissionLocation2 = 0;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    permissionLocation2 = ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE);
                }
                if (permissionLocation1 < 0 || permissionLocation2 < 0) {
                    return false;
                } else {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != 0
                            || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != 0
                            || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != 0) {

                        return false;
                    } else
                        return true;
                }

            }
        }
        return true;
    }
}