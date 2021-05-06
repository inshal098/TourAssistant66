package com.tourassistant.coderoids.helpers;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.tourassistant.coderoids.R;
import com.tourassistant.coderoids.interfaces.LoginHelperInterface;

public class PermissionHelper implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    Activity activityContext;
    public int REQUEST_PERMISSIONS = 20;
    LoginHelperInterface loginHelperInterface;
    private AlertDialog alertDialog;

    public PermissionHelper(Activity activityContext) {
        this.activityContext = activityContext;
        loginHelperInterface = (LoginHelperInterface) activityContext;
    }


    public String[] permissionsManager() {
        try {
            String[] permissions = new String[]{
                    Manifest.permission.INTERNET,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA,
                    Manifest.permission.ACCESS_NOTIFICATION_POLICY,
                    "",
                    "",
                    ""
            };
            if(Build.VERSION.SDK_INT != 30){
                permissions[8] = Manifest.permission.ACCESS_FINE_LOCATION;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && Build.VERSION.SDK_INT != 30) {
                permissions[9] = Manifest.permission.FOREGROUND_SERVICE;
                permissions[10] = Manifest.permission.ACCESS_BACKGROUND_LOCATION;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && Build.VERSION.SDK_INT != 30) {
                permissions[9] = Manifest.permission.FOREGROUND_SERVICE;
            }
            return permissions;
        } catch (Exception ex) {
        }
        return new String[]{};
    }

    public boolean checkGpsStatus() {
        LocationManager manager = (LocationManager) activityContext.getSystemService(Context.LOCATION_SERVICE);
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public void askGPSPermission() {
        try {
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activityContext);
            LayoutInflater inflater = activityContext.getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.gps_enable_view, null);
            alertDialogBuilder.setView(dialogView);
            alertDialogBuilder.setCancelable(true);
            TextView btnNo = dialogView.findViewById(R.id.tv_nothanks);
            Button turnGpsOn = dialogView.findViewById(R.id.btn_turngps_on);
            alertDialog = alertDialogBuilder.create();
            alertDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            if (!(activityContext).isFinishing()) {
                alertDialog.show();
                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom(alertDialog.getWindow().getAttributes());
                lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                lp.gravity = (Gravity.CENTER_HORIZONTAL);
                alertDialog.getWindow().setAttributes(lp);
            }
            turnGpsOn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    turnGPSOn(activityContext);
                }
            });

            btnNo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loginHelperInterface.onPrivacyPolicy("2"); // Temporary View
                    alertDialog.dismiss();
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private void turnGPSOn(Activity activityContext) {
        GoogleApiClient googleApiClient = null;
//        if (googleApiClient == null) {
        googleApiClient = new GoogleApiClient.Builder(activityContext)
                .addApi(LocationServices.API).addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();
        googleApiClient.connect();
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
//
        // **************************
        builder.setAlwaysShow(true); // this is the key ingredient
        // **************************
//
        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi
                .checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result
                        .getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can
                        // initialize location
                        // requests here.
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be
                        // fixed by showing the user
                        // a dialog.
                        try {
                            status.startResolutionForResult(activityContext, 1000);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have
                        // no way to fix the
                        // settings so we won't show the dialog.
                        break;
                }
            }
        });
//        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (!(activityContext).isFinishing()) {
            alertDialog.dismiss();
        }
        loginHelperInterface.onPrivacyPolicy("checkPermissions");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(activityContext, "Could not Complete Operation , Please Try Again", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(activityContext, "Could not Complete Operation , Please Try Again", Toast.LENGTH_SHORT).show();
    }
}

