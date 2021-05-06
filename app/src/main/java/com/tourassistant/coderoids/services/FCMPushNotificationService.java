package com.tourassistant.coderoids.services;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.tourassistant.coderoids.R;
import com.tourassistant.coderoids.home.DashboardActivity;
import com.tourassistant.coderoids.models.FireBaseRegistration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

public class FCMPushNotificationService extends FirebaseMessagingService {
    List<ActivityManager.RunningTaskInfo> services;
    private final String GROUP_KEY = "TripAssistant";
    JSONArray groupActiveNotification = new JSONArray();

    @Override

    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getData().size() > 0) {
            Map<String, String> data = remoteMessage.getData();
            JSONObject object = new JSONObject(data);
            saveAndBroadCastMessage(object);
        } else {
            String message = remoteMessage.getNotification().getBody();
            JSONObject jsonObject = new JSONObject();
            try {
                if (remoteMessage.getNotification().getTitle().matches("New Job Notification")) {
                    JSONObject jsonObject1 = new JSONObject(message);
                    String notice = jsonObject1.getString("Notice");
                    jsonObject.put("notification_id", jsonObject1.getString("notification_id"));
                    jsonObject.put("Notice", notice);
                    jsonObject.put("type", jsonObject1.getString("type"));
                    jsonObject.put("datetime", jsonObject1.getString("datetime"));
                    jsonObject.put("jobId", jsonObject1.getString("jobId"));
                }
                saveAndBroadCastMessage(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    private void saveAndBroadCastMessage(JSONObject jsonObject) {
        try {
            SharedPreferences pref = getSharedPreferences("logindata", Context.MODE_PRIVATE);
            String pushNotification_Flag = pref.getString("push_notifications", "0");
            SharedPreferences prefServerHistory = getSharedPreferences("serveraddreshistory", Context.MODE_PRIVATE);
            if (pushNotification_Flag.equals("1") || prefServerHistory.getString("push_notifications", "0").matches("1")) {

                if (jsonObject.length() > 0) {
                    ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                    services = activityManager.getRunningTasks(Integer.MAX_VALUE);
                    if (!services.get(0).topActivity.getPackageName().equalsIgnoreCase(getPackageName()) || jsonObject.getString("type").matches("newJobNotification")) {
                        sendNotification(jsonObject);
                    }

                    Intent intent = new Intent("com.pixako.trackn.refresharray");
                    intent.putExtra("message", jsonObject.getString("Notice"));
                    intent.putExtra("type", jsonObject.getString("type"));
                    intent.putExtra("notif_id", jsonObject.getString("notification_id"));
                    this.sendBroadcast(intent);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendNotification(JSONObject pushNotJObjec) {
        try {
            wakeScreen();
            groupActiveNotification.put(pushNotJObjec);
            SharedPreferences prefServerHistory = getSharedPreferences("serveraddreshistory", Context.MODE_PRIVATE);
            String siteName = prefServerHistory.getString("site_name", "");
            JSONObject jsonObject = new JSONObject(pushNotJObjec.getString("Notice"));
            String array[] = null;
            if (jsonObject.getString("date").contains(" "))
                array = jsonObject.getString("date").split(" ");
            RemoteViews notificationLayout = new RemoteViews(getPackageName(), R.layout.notification_firebase_parent);
            notificationLayout.setTextViewText(R.id.txt_job_detail, jsonObject.getString("pickup"));
            if (array != null && array[0] != null)
                notificationLayout.setTextViewText(R.id.txt_date_, array[0]);
            notificationLayout.setTextViewText(R.id.job_id, pushNotJObjec.getString("jobId"));
            if (jsonObject.has("delivery"))
                notificationLayout.setTextViewText(R.id.txt_job_detail_delivery, jsonObject.getString("delivery"));
            notificationLayout.setTextViewText(R.id.client_name, siteName);

            RemoteViews notificationSmallLayout = new RemoteViews(getPackageName(), R.layout.notification_firebase_child);
            notificationSmallLayout.setTextViewText(R.id.txt_job_detail, jsonObject.getString("pickup"));
            if (jsonObject.has("delivery"))
                notificationSmallLayout.setTextViewText(R.id.txt_job_detail_delivery, jsonObject.getString("delivery"));
            notificationSmallLayout.setTextViewText(R.id.client_name, siteName);

            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && mNotificationManager != null) {
                if (mNotificationManager.getNotificationChannels() != null) {
                    for (int i = 0; i < mNotificationManager.getNotificationChannels().size(); i++) {
                        if (mNotificationManager.getNotificationChannels().get(i).getName().toString().matches("newJobNotification")) {
                            mNotificationManager.deleteNotificationChannel(mNotificationManager.getNotificationChannels().get(i).getId());
                        }
                    }
                }
                NotificationChannel defaultChannel = new NotificationChannel(pushNotJObjec.getString("notification_id"), "newJobNotification", NotificationManager.IMPORTANCE_HIGH);
                defaultChannel.setImportance(NotificationManager.IMPORTANCE_HIGH);
                mNotificationManager.createNotificationChannel(defaultChannel);
            }
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, DashboardActivity.class)
                    .putExtra("builder", "builder")
                    .putExtra("notificationID", pushNotJObjec.getString("notification_id"))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK), 0);

            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder mSummaryBuilder = new NotificationCompat.Builder(this, pushNotJObjec.getString("notification_id"));
            mSummaryBuilder.setCustomContentView(notificationSmallLayout);
            mSummaryBuilder.setCustomBigContentView(notificationLayout);
            mSummaryBuilder.setSmallIcon(R.drawable.ic_cloudy);
            mSummaryBuilder.setTicker("New Job Assigned");
            mSummaryBuilder.setAutoCancel(true);
            mSummaryBuilder.setContentIntent(contentIntent);
            mSummaryBuilder.setChannelId(pushNotJObjec.getString("notification_id"));
            mSummaryBuilder.setSound(defaultSoundUri);
            mSummaryBuilder.setGroupSummary(true);
            mSummaryBuilder.setPriority(NotificationCompat.PRIORITY_MAX);

            mNotificationManager.notify(Integer.parseInt(pushNotJObjec.getString("notification_id")), mSummaryBuilder.build());

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("WakelockTimeout")

    private void wakeScreen() {
        @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock screenLock = ((PowerManager) getSystemService(POWER_SERVICE)).newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG");
        screenLock.acquire();
        screenLock.release();
    }



    @Override

    public void onNewToken(String newToken) {
        super.onNewToken(newToken);
        final SharedPreferences prefs = getSharedPreferences("FCMData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("refreshedToken", newToken);
        editor.apply();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        FireBaseRegistration fireBaseRegistration = new FireBaseRegistration();
        fireBaseRegistration.setToken(newToken);
        fireBaseRegistration.setTimeinMIllis(System.currentTimeMillis()+"");
        rootRef.collection("RegistrationUserId").document(firebaseUser.getUid()).set(fireBaseRegistration);
    }


}

