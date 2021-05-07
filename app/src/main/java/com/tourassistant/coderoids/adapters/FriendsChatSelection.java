package com.tourassistant.coderoids.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tourassistant.coderoids.R;
import com.tourassistant.coderoids.helpers.AppHelper;
import com.tourassistant.coderoids.interfaces.onClickListner;

import java.util.List;

public class FriendsChatSelection extends RecyclerView.Adapter<FriendsChatSelection.ViewHolder> {
    Context context;
    List<DocumentSnapshot> friendsList;
    onClickListner onClickListner;


    public FriendsChatSelection(Context applicationContext, List<DocumentSnapshot> friendsList, onClickListner onClickListner) {
        this.context = applicationContext;
        this.friendsList = friendsList;
        this.onClickListner = onClickListner;
    }

    @NonNull
    @Override
    public FriendsChatSelection.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.row_friends, viewGroup, false);
        return new FriendsChatSelection.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final FriendsChatSelection.ViewHolder viewHolder, int position) {
        position = viewHolder.getAdapterPosition();
        try {
            try {
                DocumentSnapshot documentSnapshot = friendsList.get(position);
                viewHolder.mtUserName.setText(documentSnapshot.getString("userName"));
                viewHolder.btnFollow.setText("Chat");

                int finalPosition = position;
                int finalPosition1 = position;
                viewHolder.btnFollow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onClickListner.onClick(finalPosition1,documentSnapshot);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return friendsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        MaterialTextView mtUserName;
        MaterialButton btnFollow;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mtUserName = itemView.findViewById(R.id.tv_name);
            btnFollow = itemView.findViewById(R.id.btn_follow);
        }
    }
}
