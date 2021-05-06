package com.tourassistant.coderoids.helpers;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.SparseIntArray;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

public abstract  class RuntimePermissionsActivity extends FragmentActivity {
    private SparseIntArray mErrorString;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mErrorString = new SparseIntArray();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        int permissionCheck = PackageManager.PERMISSION_GRANTED;
        StringBuilder permissionName = new StringBuilder();
        int index =0;
        for (int permission : grantResults) {
            if(!permissions[index].matches("")) {
                permissionCheck = permissionCheck + permission;
                if (permission == -1) {
                    if (permissionName.toString().matches(""))
                        permissionName = new StringBuilder(permissions[index]);
                    else
                        permissionName.append(",").append(permissions[index]);

                }
            }
            index = index +1;
        }
        if ((grantResults.length > 0) && permissionCheck == PackageManager.PERMISSION_GRANTED) {
            onPermissionsGranted(requestCode);
        } else {
            if(permissionName.toString().matches(Manifest.permission.ACCESS_BACKGROUND_LOCATION) || permissionName.toString().contains(Manifest.permission.ACCESS_FINE_LOCATION)){
                if(grantResults.length > 8 && grantResults[9] == 0 || grantResults[9] == -1){
                    onPermissionsGranted(-1);
                } else
                    onPermissionsGranted(-2);
            } else
                onPermissionsGranted(-2);
        }
    }

    public void requestAppPermissions(final String[] requestedPermissions,
                                      final int stringId, final int requestCode) {
        mErrorString.put(requestCode, stringId);
        int permissionCheck = PackageManager.PERMISSION_GRANTED;
        boolean shouldShowRequestPermissionRationale = false;
        for (String permission : requestedPermissions) {
            if (!permission.matches("")) {
                permissionCheck = permissionCheck + ContextCompat.checkSelfPermission(this, permission);
                shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale || ActivityCompat.shouldShowRequestPermissionRationale(this, permission);
            }
        }
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale) {
                try {
                    ActivityCompat.requestPermissions(RuntimePermissionsActivity.this, requestedPermissions, requestCode);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                try {
                    ActivityCompat.requestPermissions(RuntimePermissionsActivity.this, requestedPermissions, requestCode);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } else {
            onPermissionsGranted(requestCode);
        }
    }

    public abstract void onPermissionsGranted(int requestCode);
}


