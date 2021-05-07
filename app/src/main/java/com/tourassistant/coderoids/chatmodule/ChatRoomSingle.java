package com.tourassistant.coderoids.chatmodule;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import com.google.firebase.database.ValueEventListener;
import com.tourassistant.coderoids.R;
import com.tourassistant.coderoids.chatmodule.model.ChatModel;
import com.tourassistant.coderoids.chatmodule.model.ChatRoomModel;
import com.tourassistant.coderoids.helpers.AppHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ChatRoomSingle extends AppCompatActivity {

    private TextView mTextView;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room_single);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mTextView = findViewById(R.id.title);
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
                    mDatabase.child("chatMessages").child(currentChatUid).push().orderByKey().getRef().setValue(chatModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                ChatRoomModel chatRoomModel = new ChatRoomModel();
                                chatRoomModel.setChatUid(currentChatUid);
                                mDatabase.child("chats").child(currentChatUid).child("members").setValue(chatRoomModel);
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
        if(AppHelper.currentChatRecieverInstance != null) {
            currentChatUid = AppHelper.currentProfileInstance.getUserId() + "_" + AppHelper.currentChatRecieverInstance.getId();
            mDatabase.child("userChats").child(AppHelper.currentProfileInstance.getUserId()).child(currentChatUid).setValue(System.currentTimeMillis()).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    mDatabase.child("userChats").child(AppHelper.currentChatRecieverInstance.getId()).child(currentChatUid).setValue(System.currentTimeMillis());
                }
            });
        } else {
            currentChatUid = AppHelper.currentChatThreadId;
        }
    }


    public void startListening() {
        try {
            mDatabase.child("chatMessages").child(currentChatUid).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    if(snapshot != null) {
                        ChatModel dataSnapshot = snapshot.getValue(ChatModel.class);
                        if (dataSnapshot.getSentBy().matches(AppHelper.currentProfileInstance.getUserId())) {
                            chatSender.add(dataSnapshot);
                        } else {
                            chatReciever.add(dataSnapshot);
                        }
                        updateChatList();
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
        if (chatSender.size() > 0) {
            if(chatAdapterSender == null) {
                chatAdapterSender = new ChatAdapter(this, chatSender);
                rvChatListSender.setAdapter(chatAdapterSender);
                rvChatListSender.setLayoutManager(rvManSender);
            } else
                chatAdapterSender.notifyDataSetChanged();
        }
        if (chatReciever.size() > 0) {
            if(getChatAdapterReciever == null) {
                getChatAdapterReciever = new ChatAdapter(this, chatReciever);
                rvChatListReciever.setAdapter(getChatAdapterReciever);
                rvChatListReciever.setLayoutManager(rvManRec);
            } else
                getChatAdapterReciever.notifyDataSetChanged();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        chatAll = new ArrayList<>();
    }
}