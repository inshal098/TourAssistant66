package com.tourassistant.coderoids.chatmodule;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatRatingBar;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;
import com.tourassistant.coderoids.R;
import com.tourassistant.coderoids.chatmodule.model.ChatModel;

import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
    Context context;
    String type;
    ArrayList<ChatModel> chat;
    public ChatAdapter(Context applicationContext, ArrayList<ChatModel> chat) {
        this.context = applicationContext;
        this.chat = chat;
    }

    @NonNull
    @Override
    public ChatAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.row_message, viewGroup, false);
        return new ChatAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final ChatAdapter.ViewHolder viewHolder, int position) {
        position = viewHolder.getAdapterPosition();
        try {
            viewHolder.tvMessage.setText(chat.get(position).getMessage());
            viewHolder.tvMessageTime.setText(chat.get(position).getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return chat.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage ,tvMessageTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.textView_message_text);
            tvMessageTime = itemView.findViewById(R.id.textView_message_time);
        }
    }
}


