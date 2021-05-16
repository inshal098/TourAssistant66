package com.tourassistant.coderoids.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.tourassistant.coderoids.R;
import com.tourassistant.coderoids.helpers.AppHelper;
import com.tourassistant.coderoids.helpers.NotificationPublisher;
import com.tourassistant.coderoids.interfaces.onClickListner;
import com.tourassistant.coderoids.models.Profile;
import com.tourassistant.coderoids.profilefriends.FriendsProfileActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class FriendsListingAdapter extends RecyclerView.Adapter<FriendsListingAdapter.ViewHolder> {
    Context context;
    List<DocumentSnapshot> friendsList;

    public FriendsListingAdapter(Context applicationContext, List<DocumentSnapshot> friendsList) {
        this.context = applicationContext;
        this.friendsList = friendsList;
    }

    @NonNull
    @Override
    public FriendsListingAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.row_friend_listing, viewGroup, false);
        return new FriendsListingAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final FriendsListingAdapter.ViewHolder viewHolder, int position) {
        position = viewHolder.getAdapterPosition();
        try {
            DocumentSnapshot documentSnapshot = friendsList.get(position);
            String user1 = documentSnapshot.getString("userFirestoreIdReceiver");
            String user2 = documentSnapshot.getString("userFirestoreIdSender");
            String reciverId = "";
            if(AppHelper.currentProfileInstance.getUserId().matches(user1)){
                reciverId =  user2;
            } else
                reciverId = user1;

            FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
            FirebaseUser users = FirebaseAuth.getInstance().getCurrentUser();
            String uid = users.getUid();
            rootRef.collection("Users").document(reciverId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isComplete()){
                        Profile profile = task.getResult().toObject(Profile.class);
                        if(profile.getProfileImage() != null){
                            byte [] bytes=   profile.getProfileImage().toBytes();
                            Bitmap bmp = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                            viewHolder.sIVImage.setImageBitmap(bmp);
                            viewHolder.tvName.setText(profile.getDisplayName());
                        }
                    }
                }
            });

            String finalReciverId = reciverId;
            viewHolder.mCVParent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    context.startActivity(new Intent(context, FriendsProfileActivity.class).putExtra("userId", finalReciverId));
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return friendsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView sIVImage;
        TextView tvName;
        MaterialCardView mCVParent;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            sIVImage = itemView.findViewById(R.id.image);
            tvName = itemView.findViewById(R.id.tv_name);
            mCVParent = itemView.findViewById(R.id.parent_card);
        }
    }
}

