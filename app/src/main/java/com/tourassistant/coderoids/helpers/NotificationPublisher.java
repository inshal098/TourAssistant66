package com.tourassistant.coderoids.helpers;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tourassistant.coderoids.api.NotificationAPI;
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
       String id =  rootRef.collection("NotificationPool")
                .document().getId();
       rootRef.collection("NotificationPool")
                .document(id).set(publish).addOnCompleteListener(new OnCompleteListener<Void>() {
           @Override
           public void onComplete(@NonNull Task<Void> task) {
               if(task.isComplete()){
                   NotificationAPI notificationAPI = new NotificationAPI(context,notificationReciever,id);
                   notificationAPI.sendNotification();
               }
           }
       });

    }
}
