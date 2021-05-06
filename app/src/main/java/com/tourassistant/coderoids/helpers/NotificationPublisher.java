package com.tourassistant.coderoids.helpers;

import android.content.Context;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tourassistant.coderoids.models.FireBaseRegistration;
import com.tourassistant.coderoids.models.NotificationPublish;

public class NotificationPublisher {
    Context context;
    String notificationType;
    String notificationMessage;
    String notificationReciever;
    String notificaionSender;
    public NotificationPublisher (Context context , String notificationType ,  String notificationMessage , String notificationReciever){
        this.context = context;
        this.notificationType = notificationType;
        this.notificationMessage = notificationMessage;
        this.notificationReciever = notificationReciever;
    }

    public void publishNotification(){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        NotificationPublish publish = new NotificationPublish();
        publish.setNotificationMessage(notificationMessage);
        publish.setNotificationType(notificationType);
        publish.setNotificationReciever(notificationReciever);
        publish.setNotificatioSender(firebaseUser.getUid());
        publish.setNotificationTime(System.currentTimeMillis()+"");
        publish.setNotificationStatus("0");
        rootRef.collection("NotificationPool")
                .document()
                .set(publish);
    }
}
