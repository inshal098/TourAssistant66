package com.tourassistant.coderoids.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tourassistant.coderoids.R;
import com.tourassistant.coderoids.helpers.AppHelper;
import com.tourassistant.coderoids.interfaces.onClickListner;
import com.tourassistant.coderoids.models.FriendRequestModel;
import com.tourassistant.coderoids.plantrip.tripdb.TripEntity;

import java.util.List;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.ViewHolder> {
    Context context;
    List<DocumentSnapshot> friendsList;
    boolean[] rowState;
    com.tourassistant.coderoids.interfaces.onClickListner onClickListner;

    public FriendsAdapter(Context applicationContext, List<DocumentSnapshot> friendsList, onClickListner onClickListner, boolean[] rowState) {
        this.context = applicationContext;
        this.friendsList = friendsList;
        this.onClickListner = onClickListner;
        this.rowState = rowState;
    }

    @NonNull
    @Override
    public FriendsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.row_friends, viewGroup, false);
        return new FriendsAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final FriendsAdapter.ViewHolder viewHolder, int position) {
        position = viewHolder.getAdapterPosition();
        try {
            try {
                DocumentSnapshot documentSnapshot = friendsList.get(position);
                String reciverId = documentSnapshot.getString("userFirestoreIdReceiver");
                String senderId = documentSnapshot.getString("userFirestoreIdSender");
                viewHolder.mtUserName.setText(documentSnapshot.getString("userName"));
                if(rowState != null) {
                    if (rowState[position]) {
                        viewHolder.btnFollow.setText("Added");
                        viewHolder.btnFollow.setBackgroundColor(context.getResources().getColor(R.color.green));
                    } else
                        viewHolder.btnFollow.setText("Add");
                } else {
                    viewHolder.btnFollow.setVisibility(View.GONE);
                }
                int finalPosition = position;
                viewHolder.btnFollow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!rowState[finalPosition]) {
                            rowState[finalPosition] = true;
                            FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
                            String friends = "";
                            if (AppHelper.tripEntityList.getFriends() != null)
                                friends = AppHelper.tripEntityList.getFriends();
                            if (friends.matches(""))
                                friends = reciverId;
                            else
                                friends = friends + "," + reciverId;
                            AppHelper.tripEntityList.setFriends(friends);
                            rootRef.collection("Trips").document(reciverId).collection("UserTrips").document(AppHelper.tripEntityList.getFirebaseId()).set(AppHelper.tripEntityList);
                            rootRef.collection("Trips").document(senderId).collection("UserTrips").document(AppHelper.tripEntityList.getFirebaseId()).set(AppHelper.tripEntityList);
                        } else
                            Toast.makeText(context, "Invitation To this User is Already Sent", Toast.LENGTH_SHORT).show();
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
