package com.tourassistant.coderoids.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.tourassistant.coderoids.R;
import com.tourassistant.coderoids.chatmodule.ChatAdapter;
import com.tourassistant.coderoids.chatmodule.ChatRoomSingle;
import com.tourassistant.coderoids.chatmodule.model.ChatModel;
import com.tourassistant.coderoids.helpers.AppHelper;

import java.util.ArrayList;

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
            viewHolder.chatsRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(AppHelper.currentChatRecieverInstance != null)
                        AppHelper.currentChatRecieverInstance = null;
                    AppHelper.currentChatThreadId = chat.get(finalPosition);
                    context.startActivity(new Intent(context, ChatRoomSingle.class));
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return chat.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView chatsRow;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            chatsRow = itemView.findViewById(R.id.chats_row);
        }
    }
}
