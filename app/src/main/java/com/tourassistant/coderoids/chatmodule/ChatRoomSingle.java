package com.tourassistant.coderoids.chatmodule;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.tourassistant.coderoids.R;
import com.tourassistant.coderoids.chatmodule.model.ChatModel;
import com.tourassistant.coderoids.chatmodule.model.ChatRoomModel;
import com.tourassistant.coderoids.helpers.AppHelper;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class ChatRoomSingle extends AppCompatActivity {

    private TextView chatTitle;
    private RecyclerView rvChatListReciever, rvChatListSender;
    private ImageButton sendMessage;
    private TextInputEditText etMessage;
    LinearLayoutManager rvManRec, rvManSender;
    ChatAdapter chatAdapterSender, getChatAdapterReciever;
    ArrayList<ChatModel> chatSender = new ArrayList<>();
    ArrayList<ChatModel> chatReciever = new ArrayList<>();
    ArrayList<ChatModel> chatAll;
    DatabaseReference mDatabase;
    String currentChatUid;
    int groupNodeIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room_single);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        chatTitle = findViewById(R.id.chatTitle);
        etMessage = findViewById(R.id.editText_message);
        sendMessage = findViewById(R.id.imageView_send);

        //Recycelr View
        rvChatListReciever = findViewById(R.id.recycler_view_messages_rec);
        rvChatListSender = findViewById(R.id.recycler_view_messages_sender);
        rvManRec = new LinearLayoutManager(this);
        rvManRec.setOrientation(LinearLayoutManager.VERTICAL);
        rvManSender = new LinearLayoutManager(this);
        rvManSender.setOrientation(LinearLayoutManager.VERTICAL);

        if (chatSender == null) {
            chatSender = new ArrayList<>();
        }
        if (chatReciever == null) {
            chatReciever = new ArrayList<>();
        }
        manageChatNode();
        startListening();
        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!etMessage.toString().matches("") && !currentChatUid.matches("")) {
                    String currTime = System.currentTimeMillis() + "";
                    ChatModel chatModel = new ChatModel();
                    chatModel.setMessage(etMessage.getText().toString());
                    String time = AppHelper.getDateTime();
                    chatModel.setTime(time);
                    chatModel.setSentBy(AppHelper.currentProfileInstance.getUserId());
                    chatModel.setId(currTime);
                    chatModel.setName(AppHelper.currentProfileInstance.getDisplayName());
                    mDatabase.child("chatMessages").child(currentChatUid).push().orderByKey().getRef().setValue(chatModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                ChatRoomModel chatRoomModel = new ChatRoomModel();
                                chatRoomModel.setChatUid(currentChatUid);
                                mDatabase.child("chats").child(currentChatUid).child("members").setValue(chatRoomModel);
                                etMessage.setText("");
                            }
                        }
                    });
//                    mDatabase.child(AppHelper.currentProfileInstance.getUserId()).child("messages").push().setValue(chatModel).addOnCompleteListener(new OnCompleteListener<Void>() {
//                        @Override
//                        public void onComplete(@NonNull Task<Void> task) {
//                            if(task.isComplete()){
//                                task.getResult();
//                            }
//
//
//                        }
//                    });
                }
            }
        });
    }


    private void manageChatNode() {
        try {
            if (AppHelper.currentChatRecieverInstance != null) {
                chatTitle.setText("Chat Room");
                currentChatUid = AppHelper.currentProfileInstance.getUserId() + "_" + AppHelper.currentChatRecieverInstance.getId();
                mDatabase.child("userChats").child(AppHelper.currentProfileInstance.getUserId()).child(currentChatUid).setValue(System.currentTimeMillis()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        mDatabase.child("userChats").child(AppHelper.currentChatRecieverInstance.getId()).child(currentChatUid).setValue(System.currentTimeMillis());
                    }
                });
            } else if (AppHelper.groupChatRecieversInstance != null) {
                chatTitle.setText(AppHelper.tripEntityList.getTripTitle() +" Chat Room");
                if (!AppHelper.tripEntityList.getJoinTripRequests().matches("") && AppHelper.tripEntityList.getJoinTripRequests() != null) {
                    JSONArray jsonArray = new JSONArray(AppHelper.tripEntityList.getJoinTripRequests());
                    String groupChatId = "";
                    for (int i = 0; i < jsonArray.length(); i++) {
                        if (groupChatId.matches("")) {
                            groupChatId = jsonArray.getJSONObject(i).getString("userId");
                        } else {
                            groupChatId = groupChatId + "_" + jsonArray.getJSONObject(i).getString("userId");
                        }
                    }
                    if (!groupChatId.contains(AppHelper.currentProfileInstance.getUserId())) {
                        groupChatId = groupChatId + "_" + AppHelper.currentProfileInstance.getUserId();
                    }
                    currentChatUid = groupChatId;

                    mDatabase.child("userChats").child(AppHelper.currentProfileInstance.getUserId()).child(currentChatUid).setValue(System.currentTimeMillis()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            createAndManageGroupNodeUsers(mDatabase, currentChatUid, jsonArray);
                        }
                    });
                }
            } else {
                currentChatUid = AppHelper.currentChatThreadId;
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
        }

    }

    private void createAndManageGroupNodeUsers(DatabaseReference mDatabase, String currentChatUid, JSONArray jsonArray) {
        try {
            if (groupNodeIndex == -1)
                groupNodeIndex = 0;
            else
                groupNodeIndex = groupNodeIndex + 1;
            if (groupNodeIndex <= jsonArray.length()) {
                String userId = jsonArray.getJSONObject(groupNodeIndex).getString("userId");
                mDatabase.child("userChats").child(userId).child(currentChatUid).setValue(System.currentTimeMillis()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        createAndManageGroupNodeUsers(mDatabase, currentChatUid, jsonArray);
                    }
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void startListening() {
        try {
            mDatabase.child("chatMessages").child(currentChatUid).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    if (snapshot != null) {
                        ChatModel dataSnapshot = snapshot.getValue(ChatModel.class);
                        if (dataSnapshot.getId() != null) {
                            if (dataSnapshot.getSentBy().matches(AppHelper.currentProfileInstance.getUserId())) {
                                dataSnapshot.setType("S");
                            } else {
                                dataSnapshot.setType("R");
                            }
                            if (chatAll == null)
                                chatAll = new ArrayList<>();
                            chatAll.add(dataSnapshot);
                            updateChatList();
                        }
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    DataSnapshot dataSnapshot = snapshot;

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                    DataSnapshot dataSnapshot = snapshot;

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    DataSnapshot dataSnapshot = snapshot;

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    DatabaseError dataSnapshot = error;

                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    private void updateChatList() {
        chatAdapterSender = new ChatAdapter(this, chatAll, "A");
        rvChatListSender.setAdapter(chatAdapterSender);
        rvChatListSender.setLayoutManager(rvManSender);
//        if (chatSender.size() > 0) {
//            if(chatAdapterSender == null) {
//                chatAdapterSender = new ChatAdapter(this, chatSender,"S");
//                rvChatListSender.setAdapter(chatAdapterSender);
//                rvChatListSender.setLayoutManager(rvManSender);
//            } else
//                chatAdapterSender.notifyDataSetChanged();
//        }
//        if (chatReciever.size() > 0) {
//            if(getChatAdapterReciever == null) {
//                getChatAdapterReciever = new ChatAdapter(this, chatReciever,"R");
//                rvChatListReciever.setAdapter(getChatAdapterReciever);
//                rvChatListReciever.setLayoutManager(rvManRec);
//            } else
//                getChatAdapterReciever.notifyDataSetChanged();
//        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        chatAll = new ArrayList<>();
        AppHelper.currentChatRecieverInstance = null;
        AppHelper.groupChatRecieversInstance = null;
    }
}