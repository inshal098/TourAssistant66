package com.tourassistant.coderoids.helpers;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.libraries.places.api.model.Place;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tourassistant.coderoids.models.PlacesModel;
import com.tourassistant.coderoids.models.Profile;
import com.tourassistant.coderoids.plantrip.tripdb.TripEntity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/* *
 * Created by developer on 5/20/2015.
 */
public class AppHelper {
    public static  int imageHeight = 500, imageWidth = 700;
    public static String fragmentLocation = "";
    public static List<DocumentSnapshot> filteredTrips = new ArrayList<>();
    public static List<DocumentSnapshot> allTrips = new ArrayList<>();
    public static List<DocumentSnapshot> allUsers = new ArrayList<>();
    public static List<DocumentSnapshot> allFriends = new ArrayList<>();
    public static List<DocumentSnapshot> friendRequests = new ArrayList<>();
    public static DocumentSnapshot tripRoomSnap ;
    public static Profile currentProfileInstance;
    public static double lastSearchLat;
    public static double lastSearchLon;
    public static String lastSearchAddress;
    public static String lastCoordinates;
    public static Location location;
    public static Place selectedPlace;
    public static ArrayList<Place> tripRoomPlace;
    public static DocumentSnapshot currentChatRecieverInstance;
    public static ArrayList<DocumentSnapshot> groupChatRecieversInstance;
    public static String currentChatThreadId;
    public static String inProgressTripId;
    public static PlacesModel editDestModel;
    public static List<DocumentSnapshot> newsListCurrent;
    private static AppHelper instance = null;
    public static Context context;
    public static TripEntity tripEntityList = new TripEntity();
    public static JSONArray interestUser = new JSONArray();
    private static final String defaultDateTimeFormat = "yyyy-MM-dd HH:mm:ss";
    public AppHelper(Context mContext) {
        context = mContext;
    }

    public static AppHelper getInstance(Context mContext) {
        if (instance == null) {
            instance = new AppHelper(mContext);
        }
        context = mContext;
        return instance;
    }

    public static String getFragmentLocation() {
        return fragmentLocation;
    }

    public static void setFragmentLocation(String fragmentLocation) {
        AppHelper.fragmentLocation = fragmentLocation;
    }

    public static String encodedString(String value) {
        return Uri.encode(value);
    }

    public static String decodedString(String value) {
        return Uri.decode(value);
    }

    public static String checkStringIsNull(JSONObject jsonObject, String parameter) {
        String value = "";
        try {
            if (jsonObject.has(parameter)) {
                if (!jsonObject.isNull(parameter) && !jsonObject.getString(parameter).equals("[]"))
                    value = jsonObject.getString(parameter);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return value;
    }

    public static String checkStringIsNull(JSONObject jsonObject, String parameter, String defaultValue) {
        String value = defaultValue;
        try {
            if (jsonObject.has(parameter)) {
                if (!jsonObject.isNull(parameter) && !jsonObject.getString(parameter).equals("[]"))
                    value = jsonObject.getString(parameter);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }


    public static String getDateTime() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());
        return df.format(c.getTime());
    }

    public static String getCreateJobFormat() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy", java.util.Locale.getDefault());
        return df.format(c.getTime());
    }

    public static String getDateTime(int increment) {

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, increment);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());
        return df.format(calendar.getTime());
    }

    public static String getDate() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());

        return df.format(c.getTime());
    }

    public static String getTime() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());

        return df.format(c.getTime());
    }



    public static String removeSpaceInTime(String time) {
        return time.replace(" ", "%20");
    }

    public static boolean isNetworkAvailable(Activity activity) {
        boolean flag = false;
        ConnectivityManager cwjManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cwjManager.getActiveNetworkInfo() != null)
            flag = cwjManager.getActiveNetworkInfo().isAvailable();
        return flag;
    }

    public static void runAsyncTaskWithout(AsyncTask task) {
        try {
            if (task.getStatus() != AsyncTask.Status.RUNNING)
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public static void runAsyncTask(AsyncTask task, String... params) {
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
    }

    public static boolean isValidEmail(CharSequence target) {
        if (target == null) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

    public static boolean isNetworkAvailable(Context activity) {
        boolean flag = false;
        ConnectivityManager cwjManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cwjManager.getActiveNetworkInfo() != null)
            flag = cwjManager.getActiveNetworkInfo().isAvailable();
        return flag;
    }

    public static String convertMilliToDateTime(long milliSeconds) {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(defaultDateTimeFormat, java.util.Locale.getDefault());

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    public static String getUserIntrests(JSONArray intrests) {
        String interest = "";
        if(intrests != null && !intrests.toString().matches("")){
            for(int i=0; i<intrests.length();i++){
                try {
                    JSONObject jsonObject = intrests.getJSONObject(i);
                    if(interest.matches(""))
                        interest = jsonObject.getString("interestName");
                    else
                        interest = interest +" | " + jsonObject.getString("interestName");
                } catch (JSONException ex){
                    ex.printStackTrace();
                }

            }
        }
        return interest;
    }


    public File createFolder(String subDirectory, String fileName, Bitmap imageToSave, Activity activity, String customerID, boolean isHighRes) {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), AppConstants.MAIN_FOLDER + subDirectory);

        if (!mediaStorageDir.exists()) {
            mediaStorageDir.mkdirs();
        }

        File mediaFile  = new File(mediaStorageDir.getPath() + File.separator + fileName + customerID + ".jpg");
        try {
            FileOutputStream out = new FileOutputStream(mediaFile);
            imageToSave.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                activity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/TrackN/" + subDirectory)));
            } else {
                activity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/TrackN/" + subDirectory)));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return mediaFile;
    }

    public static void saveBitmapToJpg(Bitmap bitmap, File file, int dpi) throws IOException {
        ByteArrayOutputStream imageByteArray = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, imageByteArray);
        byte[] imageData = imageByteArray.toByteArray();

        setDpi(imageData, dpi);

        FileOutputStream fileOutputStream = new FileOutputStream(file);
        fileOutputStream.write(imageData);
        fileOutputStream.close();
    }

    private static void setDpi(byte[] imageData, int dpi) {
        imageData[13] = 1;
        imageData[14] = (byte) (dpi >> 8);
        imageData[15] = (byte) (dpi & 0xff);
        imageData[16] = (byte) (dpi >> 8);
        imageData[17] = (byte) (dpi & 0xff);
    }

    public static File getCurrentDirectory(String subFolderName) {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), AppConstants.MAIN_FOLDER + File.separator + subFolderName);
        if (!mediaStorageDir.exists()) {
            mediaStorageDir.mkdirs();
        }
        return mediaStorageDir;
    }




    public void hideKeyBoard(Activity activity) {
        if (activity.getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }

    public void hideKeyBoard(final AutoCompleteTextView editText) {
        editText.setInputType(0);
        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.setCursorVisible(true);
                editText.requestFocus();
                editText.setInputType(InputType.TYPE_CLASS_TEXT);
            }
        });
    }

    public void setEditTextFocus(final EditText editText, boolean isFocused) {
        editText.setCursorVisible(isFocused);
        editText.setFocusable(isFocused);
        editText.setFocusableInTouchMode(isFocused);
        if (isFocused) {
            editText.requestFocus();
            InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
        } else {
            InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(editText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }

        editText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                setEditTextFocus(editText, true);
                return false;
            }
        });
        // for AutoCompleteTexView as not supported onTouchListener
        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setEditTextFocus(editText, true);
            }
        });
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                setEditTextFocus(editText, false);
                return false;
            }
        });
    }

    public static void clearCache(Context context) {
        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (String aChildren : children) {
                boolean success = deleteDir(new File(dir, aChildren));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }

    public static Profile getUserProfileObj(String id){
        for (int i = 0; i < AppHelper.allUsers.size(); i++) {
            if(AppHelper.currentProfileInstance != null && id.matches(AppHelper.currentProfileInstance.getUserId()))
                return  AppHelper.currentProfileInstance;
            if (id.matches(AppHelper.allUsers.get(i).getId())) {
                Profile profile = AppHelper.allUsers.get(i).toObject(Profile.class);
                return profile;
            }
        }
        return new Profile();
    }

}
