package com.tourassistant.coderoids.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tourassistant.coderoids.R;
import com.tourassistant.coderoids.helpers.AppHelper;
import com.tourassistant.coderoids.helpers.NotificationPublisher;
import com.tourassistant.coderoids.interfaces.onClickListner;
import com.tourassistant.coderoids.models.FriendRequestModel;
import com.tourassistant.coderoids.models.NotificationPublish;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class SuggestedFriendsAdapter extends RecyclerView.Adapter<SuggestedFriendsAdapter.ViewHolder> {
    Context context;
    List<DocumentSnapshot> friendsList;
    boolean[] rowState;
    onClickListner onClickListner;
    ProgressDialog progressDialog;
    public SuggestedFriendsAdapter(Context applicationContext, List<DocumentSnapshot> friendsList, onClickListner onClickListner, ProgressDialog progressDialog) {
        this.context = applicationContext;
        this.friendsList = friendsList;
        this.onClickListner = onClickListner;
        this.progressDialog = progressDialog;
    }

    @NonNull
    @Override
    public SuggestedFriendsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.row_friends, viewGroup, false);
        return new SuggestedFriendsAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final SuggestedFriendsAdapter.ViewHolder viewHolder, int position) {
        position = viewHolder.getAdapterPosition();
        try {
            try {
                DocumentSnapshot documentSnapshot = friendsList.get(position);
                viewHolder.mtUserName.setText(documentSnapshot.getString("userName"));
                int finalPosition = position;
                viewHolder.btnFollow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        progressDialog.setMessage("Please Wait...");
                        progressDialog.setIndeterminate(true);
                        progressDialog.show();
                        String uid = documentSnapshot.getId();
                        String currentID = AppHelper.currentProfileInstance.getUserId();
                        FriendRequestModel friendRequestModel = new FriendRequestModel();
                        friendRequestModel.setUserEmail(AppHelper.currentProfileInstance.getEmail());
                        friendRequestModel.setUserFirestoreIdSender(AppHelper.currentProfileInstance.getUserId());
                        friendRequestModel.setUserName(AppHelper.currentProfileInstance.getUserName());
                        friendRequestModel.setUserFirestoreIdReceiver(uid);
                        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
                        DocumentReference uidRef = rootRef.collection("Users").document(uid).collection("FriendRequestsReceived").document();
                        String getRequestId = uidRef.getId();
                        friendRequestModel.setFriendRequestId(getRequestId);
                        uidRef.set(friendRequestModel);
                        DocumentReference uidRef2 = rootRef.collection("Users").document(currentID).collection("FriendRequestSent").document(getRequestId);
                        uidRef2.set(friendRequestModel);
                        JSONArray jsonArray = new JSONArray();
                        JSONObject notiObjNew = new JSONObject();
                        try {
                            notiObjNew.put("id", AppHelper.tripEntityList.getFirebaseId());
                            notiObjNew.put("friendRequesSenderId", currentID );
                            notiObjNew.put("message", "You Have Received A Follow Request");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        jsonArray.put(notiObjNew);
                        NotificationPublisher notificationPublisher = new NotificationPublisher(context, "FollowRequest", jsonArray + "", uid);
                        notificationPublisher.publishNotification();
                        onClickListner.onClick(finalPosition,null);

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


