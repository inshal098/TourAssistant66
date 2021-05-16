package com.tourassistant.coderoids.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.tourassistant.coderoids.R;
import com.tourassistant.coderoids.chatmodule.ChatAdapter;
import com.tourassistant.coderoids.chatmodule.ChatRoomSingle;
import com.tourassistant.coderoids.chatmodule.model.ChatModel;
import com.tourassistant.coderoids.helpers.AppHelper;
import com.tourassistant.coderoids.models.Profile;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatListsAdapter extends RecyclerView.Adapter<ChatListsAdapter.ViewHolder> {
    Context context;
    String type;
    ArrayList<String> chat;

    public ChatListsAdapter(Context applicationContext, ArrayList<String> chat) {
        this.context = applicationContext;
        this.chat = chat;
    }

    @NonNull
    @Override
    public ChatListsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.row_chat, viewGroup, false);
        return new ChatListsAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final ChatListsAdapter.ViewHolder viewHolder, int position) {
        position = viewHolder.getAdapterPosition();
        try {
            int finalPosition = position;
            List<Profile> profile = fetchRecieverId(chat.get(position));
            if(profile != null) {
                String membersName = "";
                for(int i=0 ; i<profile.size(); i++) {
                    if(membersName.matches("")) {
                        membersName = profile.get(i).getDisplayName();
                    } else
                        membersName = membersName+","+profile.get(i).getDisplayName();
                        viewHolder.chatDesc.setText(membersName);
                    if (i==0 && profile.get(0).getProfileImage() != null) {
                        byte[] bytes = profile.get(0).getProfileImage().toBytes();
                        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        viewHolder.profileImage.setImageBitmap(bmp);
                    }
                }
            }
            viewHolder.chatsRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (AppHelper.currentChatRecieverInstance != null)
                        AppHelper.currentChatRecieverInstance = null;
                    AppHelper.currentChatThreadId = chat.get(finalPosition);
                    context.startActivity(new Intent(context, ChatRoomSingle.class));
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<Profile> fetchRecieverId(String s) {
        DocumentSnapshot documentSnapshot = null;
        List<Profile> profile = new ArrayList<>();
        if (AppHelper.allUsers != null && AppHelper.allUsers.size() > 0) {
            String recieverId = "";
            String chatId[] = s.split("_");
            if(chatId.length > 2){
                for(int l=0 ;l<chatId.length;l++) {
                    recieverId =  chatId[l];
                    for (int i = 0; i < AppHelper.allUsers.size(); i++) {
                        if (recieverId.matches(AppHelper.allUsers.get(i).getId())) {
                            documentSnapshot = AppHelper.allUsers.get(i);
                            profile.add(documentSnapshot.toObject(Profile.class));
                        }
                    }
                }
            } else {
                if (chatId != null && chatId.length > 1) {
                    if (chatId[0].matches(AppHelper.currentProfileInstance.getUserId())) {
                        recieverId = chatId[1];
                    } else {
                        recieverId = chatId[0];
                    }
                }
                for (int i = 0; i < AppHelper.allUsers.size(); i++) {
                    if (recieverId.matches(AppHelper.allUsers.get(i).getId())) {
                        documentSnapshot = AppHelper.allUsers.get(i);
                        profile.add(documentSnapshot.toObject(Profile.class));
                    }
                }
            }

        }
        return profile;
    }

    @Override
    public int getItemCount() {
        return chat.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView chatsRow;
        TextView chatDesc;
        CircleImageView profileImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            chatsRow = itemView.findViewById(R.id.chats_row);
            chatDesc = itemView.findViewById(R.id.chat_desc);
            profileImage = itemView.findViewById(R.id.profile_photo);
        }
    }
}
