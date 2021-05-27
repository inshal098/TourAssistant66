package com.tourassistant.coderoids.chatmodule;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.tourassistant.coderoids.R;
import com.tourassistant.coderoids.adapters.ChatListsAdapter;
import com.tourassistant.coderoids.adapters.FriendsChatSelection;
import com.tourassistant.coderoids.chatmodule.model.ChatModel;
import com.tourassistant.coderoids.helpers.AppHelper;
import com.tourassistant.coderoids.interfaces.onClickListner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ChatParentActivity extends AppCompatActivity implements onClickListner {
    RecyclerView chatList;
    TextView noChatMessage;
    LinearLayoutManager rvManRec;
    ExtendedFloatingActionButton newMessage;
    boolean rowState[];
    onClickListner onClickListner;
    AlertDialog alertDialog;
    DatabaseReference mDatabase;
    ArrayList<String> allChatsIds = new ArrayList<>();
    String type = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_parent);
        type = getIntent().getStringExtra("type");
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        onClickListner = (onClickListner) this;
        newMessage = findViewById(R.id.add_message);
        chatList = findViewById(R.id.chat_list);
        noChatMessage = findViewById(R.id.no_chats);
        rvManRec = new LinearLayoutManager(this);
        rvManRec.setOrientation(LinearLayoutManager.VERTICAL);

        newMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(AppHelper.allUsers!= null && AppHelper.allUsers.size()>0){
                    showAlertDialog();
                }
            }
        });
        manageFriends();
        fetchAllUserChats();
        if(type == null && AppHelper.currentProfileInstance.getUserId().matches(AppHelper.tripEntityList.getFirebaseUserId()))
            manageGroupChat();


    }

    private void manageGroupChat() {
        try {
            if(AppHelper.allUsers.size()>0) {
                if (!AppHelper.tripEntityList.getJoinTripRequests().matches("") && AppHelper.tripEntityList.getJoinTripRequests() != null) {
                    JSONArray jsonArray = new JSONArray(AppHelper.tripEntityList.getJoinTripRequests());
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

                    for (int i = 0; i < AppHelper.allUsers.size(); i++) {
                        for (int j = 0; j <jsonArray.length(); j++){
                            String userID = "";
                            JSONObject jsonObject = jsonArray.getJSONObject(j);
                            userID = jsonObject.getString("userId");
                            if(userID.matches(AppHelper.allUsers.get(i).getId()) && !currentUser.getUid().matches(AppHelper.allUsers.get(i).getId())){
                                if(AppHelper.groupChatRecieversInstance == null)
                                    AppHelper.groupChatRecieversInstance = new ArrayList<>();
                                AppHelper.groupChatRecieversInstance.add(AppHelper.allUsers.get(i));
                            }
                        }
                    }
                    AppHelper.currentChatRecieverInstance = null;
                    startActivity(new Intent(ChatParentActivity.this,ChatRoomSingle.class));
                    finish();

                }
            }
        } catch (Exception ex){
            ex.printStackTrace();
        }

    }

    private void fetchAllUserChats() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("userChats").child(AppHelper.currentProfileInstance.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot != null){
                    HashMap hashMap = (HashMap) snapshot.getValue();
                    hashMap.size();
                    Iterator itr = hashMap.entrySet().iterator();
                    while (itr.hasNext()) {
                        Map.Entry pair = (Map.Entry) itr.next();
                        Object pairName = pair.getKey();
                        if(pairName.toString().contains("_")){
                            allChatsIds.add(pairName.toString());
                        }
                    }
                    if(allChatsIds.size() > 0){
                        noChatMessage.setVisibility(View.GONE);
                        manageAllChatsThreads(allChatsIds);
                    } else
                        noChatMessage.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void manageAllChatsThreads(ArrayList<String> allChatsIds) {
        ChatListsAdapter chatListsAdapter = new ChatListsAdapter(this,allChatsIds);
        chatList.setAdapter(chatListsAdapter);
        chatList.setLayoutManager(rvManRec);

    }


    private void manageFriends() {
        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        rootRef.collection("Users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
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
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void showAlertDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.friends_listing, null);
        dialogBuilder.setView(dialogView);
        alertDialog = dialogBuilder.create();
        RecyclerView rvFriends = dialogView.findViewById(R.id.friends_list_rv);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        FriendsChatSelection  friendsChatSelection = new FriendsChatSelection(this,AppHelper.allUsers,onClickListner);
        rvFriends.setAdapter(friendsChatSelection);
        rvFriends.setLayoutManager(linearLayoutManager);
        alertDialog.show();
    }

    @Override
    public void onClick(int pos, DocumentSnapshot documentSnapshot , String tag) {
        AppHelper.currentChatRecieverInstance = documentSnapshot;
        if(alertDialog != null && alertDialog.isShowing()){
            alertDialog.dismiss();
        }
        startActivity(new Intent(ChatParentActivity.this,ChatRoomSingle.class));
    }
}