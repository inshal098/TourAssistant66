package com.tourassistant.coderoids.chatmodule;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
    public ChatAdapter(Context applicationContext, ArrayList<ChatModel> chat,String type) {
        this.context = applicationContext;
        this.chat = chat;
        this.type = type;
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
            if(chat.get(position).getType().matches("S")){
                viewHolder.llSend.setVisibility(View.VISIBLE);
                viewHolder.llRec.setVisibility(View.GONE);
                viewHolder.tvMessage.setText(chat.get(position).getMessage());
                viewHolder.tvMessageTime.setText(chat.get(position).getTime());
                viewHolder.tvName2.setText(chat.get(position).getName());
            } else {
                viewHolder.llRec.setVisibility(View.VISIBLE);
                viewHolder.llSend.setVisibility(View.GONE);
                viewHolder.tvMessageRec.setText(chat.get(position).getMessage());
                viewHolder.tvMessageTimeRec.setText(chat.get(position).getTime());
                viewHolder.tvName.setText(chat.get(position).getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return chat.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage ,tvMessageTime , tvMessageRec , tvMessageTimeRec ,tvName , tvName2;
        RelativeLayout message_view;
        LinearLayout llRec ,llSend;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.name);
            tvName2 = itemView.findViewById(R.id.name2);
            tvMessageRec = itemView.findViewById(R.id.textView_message_text_rec);
            tvMessage = itemView.findViewById(R.id.textView_message_text);
            tvMessageTimeRec = itemView.findViewById(R.id.textView_message_time_rec);
            tvMessageTime = itemView.findViewById(R.id.textView_message_time);
            message_view = itemView.findViewById(R.id.message_view);
            llRec = itemView.findViewById(R.id.ll_rec);
            llSend = itemView.findViewById(R.id.ll_send);
        }
    }
}


