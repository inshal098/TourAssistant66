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
import com.tourassistant.coderoids.helpers.AppHelper;
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
        }
    }

    private void saveAndBroadCastMessage(JSONObject jsonObject) {
        try {
            sendNotification(jsonObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendNotification(JSONObject pushNotJObjec) {
        try {
            wakeScreen();
            groupActiveNotification.put(pushNotJObjec);
            JSONArray jsonArray = new JSONArray(pushNotJObjec.getString("notificationMessage"));
            JSONObject jsonObject = jsonArray.getJSONObject(0);
            String date  = AppHelper.convertMilliToDateTime(Long.parseLong(pushNotJObjec.getString("notificationTime")));
            RemoteViews notificationLayout = new RemoteViews(getPackageName(), R.layout.notification_firebase_parent);
            notificationLayout.setTextViewText(R.id.txt_job_detail, jsonObject.getString("message"));
            notificationLayout.setTextViewText(R.id.txt_date_, date);
            notificationLayout.setTextViewText(R.id.job_id, "Message");

            RemoteViews notificationSmallLayout = new RemoteViews(getPackageName(), R.layout.notification_firebase_child);
            notificationSmallLayout.setTextViewText(R.id.txt_job_detail, jsonObject.getString("message"));

            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && mNotificationManager != null) {
                if (mNotificationManager.getNotificationChannels() != null) {
                    for (int i = 0; i < mNotificationManager.getNotificationChannels().size(); i++) {
                        if (mNotificationManager.getNotificationChannels().get(i).getName().toString().matches("TripAssistantNotifications")) {
                            mNotificationManager.deleteNotificationChannel(mNotificationManager.getNotificationChannels().get(i).getId());
                        }
                    }
                }
                NotificationChannel defaultChannel = new NotificationChannel(pushNotJObjec.getString("notificationTime"), "TripAssistantNotifications", NotificationManager.IMPORTANCE_HIGH);
                defaultChannel.setImportance(NotificationManager.IMPORTANCE_HIGH);
                mNotificationManager.createNotificationChannel(defaultChannel);
            }
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, DashboardActivity.class)
                    .putExtra("builder", "builder")
                    .putExtra("notificationID", pushNotJObjec.getString("notificationTime"))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK), 0);

            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder mSummaryBuilder = new NotificationCompat.Builder(this, pushNotJObjec.getString("notificationTime"));
            mSummaryBuilder.setCustomContentView(notificationSmallLayout);
            mSummaryBuilder.setCustomBigContentView(notificationLayout);
            mSummaryBuilder.setSmallIcon(R.drawable.ic_cloudy);
            mSummaryBuilder.setTicker("New Message");
            mSummaryBuilder.setAutoCancel(true);
            mSummaryBuilder.setContentIntent(contentIntent);
            mSummaryBuilder.setChannelId(pushNotJObjec.getString("notificationTime"));
            mSummaryBuilder.setSound(defaultSoundUri);
            mSummaryBuilder.setGroupSummary(true);
            mSummaryBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
            mNotificationManager.notify(1, mSummaryBuilder.build());
            Intent intent = new Intent("com.coderoids.notification");
            intent.putExtra("message", pushNotJObjec+"");
            intent.putExtra("notificationType", pushNotJObjec.getString("notificationType"));
            intent.putExtra("notif_id", 1);
            this.sendBroadcast(intent);
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
        if(firebaseUser != null && firebaseUser.getUid() != null)
            rootRef.collection("RegistrationUserId").document(firebaseUser.getUid()).set(fireBaseRegistration);
    }


}

