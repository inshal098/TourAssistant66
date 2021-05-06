package com.tourassistant.coderoids.home;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tourassistant.coderoids.models.NotificationPublish;

import java.util.HashMap;

public class EventListnerManager {
    Context contex;
    public EventListnerManager(Context context) {
        this.contex = context;
    }

    public void startListening() {
        try {
            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
            mDatabase.child("NotificationPool");
            ValueEventListener postListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    manageAndPublishNotification(dataSnapshot);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    DatabaseError databaseError = error;
                }
            };
            mDatabase.addValueEventListener(postListener);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void manageAndPublishNotification(DataSnapshot dataSnapshot) {
        for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
            NotificationPublish value = postSnapshot.getValue(NotificationPublish.class);
            Intent intent = new Intent("com.coderoids.notification");
            intent.putExtra("message", value.getNotificationMessage());
            intent.putExtra("type", value.getNotificationType());
            intent.putExtra("notif_id", value.getNotificationTime());
            contex.sendBroadcast(intent);
        }
    }

    public void initializeBroadCastRec(BroadcastReceiver receiver) {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Toast.makeText(context,"Message Recieved",Toast.LENGTH_SHORT).show();
            }
        };
    }
}
