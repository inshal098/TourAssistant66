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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class FollowRequestAdapter extends RecyclerView.Adapter<FollowRequestAdapter.ViewHolder> {
    Context context;
    JSONArray intestArr;
    LayoutInflater inflter;
    List<DocumentSnapshot> friendsRequest;
    boolean[] rowState;
    onClickListner  onClickListner;
    ProgressDialog progressDialog;
    public FollowRequestAdapter(Context applicationContext, List<DocumentSnapshot> friendsRequest, boolean[] rowState , onClickListner  onClickListner, ProgressDialog progressDialog) {
        this.context = applicationContext;
        this.friendsRequest = friendsRequest;
        inflter = (LayoutInflater.from(applicationContext));
        this.rowState = rowState;
        this.onClickListner = onClickListner;
        this.progressDialog = progressDialog;

    }

    @NonNull
    @Override
    public FollowRequestAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.row_friends, viewGroup, false);
        return new FollowRequestAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final FollowRequestAdapter.ViewHolder viewHolder, int position) {
        position = viewHolder.getAdapterPosition();
        try {
            DocumentSnapshot documentSnapshot = friendsRequest.get(position);
            viewHolder.mtUserName.setText(documentSnapshot.getString("userName"));
            viewHolder.btnFollow.setText("Accept");
            viewHolder.btnFollow.setBackgroundColor(context.getResources().getColor(R.color.green));

            int finalPosition = position;
            viewHolder.btnFollow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    progressDialog.setMessage("Please Wait...");
                    progressDialog.setIndeterminate(true);
                    progressDialog.show();
                    String friendRequestId = documentSnapshot.getString("friendRequestId");
                    String currentID = AppHelper.currentProfileInstance.getUserId();
                    String friendRequestSenderId = documentSnapshot.getString("userFirestoreIdSender");;
                    FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
                    FriendRequestModel friendRequestModel = new FriendRequestModel();
                    friendRequestModel.setUserEmail(documentSnapshot.getString("userEmail"));
                    friendRequestModel.setUserFirestoreIdSender(documentSnapshot.getString("userFirestoreIdSender"));
                    friendRequestModel.setUserFirestoreIdReceiver(documentSnapshot.getString("userFirestoreIdReceiver"));
                    friendRequestModel.setUserName(documentSnapshot.getString("userName"));
                    friendRequestModel.setFriendRequestId(documentSnapshot.getString("friendRequestId"));
                    //updating current user friends List
                    DocumentReference uidRef = rootRef.collection("Users").document(currentID).collection("Friends").document(friendRequestId);
                    uidRef.set(friendRequestModel);
                    friendRequestModel.setUserName(AppHelper.currentProfileInstance.getUserName());
                    friendRequestModel.setUserEmail(AppHelper.currentProfileInstance.getEmail());
                    //Updating sender's Friend Lis
                    DocumentReference uidRef1 = rootRef.collection("Users").document(friendRequestSenderId).collection("Friends").document(friendRequestId);
                    uidRef1.set(friendRequestModel);
                    //Deleting Entry from Friend Requests Received
                    DocumentReference uidRef2 = rootRef.collection("Users").document(currentID).collection("FriendRequestsReceived").document(friendRequestId);
                    uidRef2.delete();

                    //Deleting Entry from Friend Requests Received
                    DocumentReference uidRef3 = rootRef.collection("Users").document(friendRequestSenderId).collection("FriendRequestSent").document(friendRequestId);
                    uidRef3.delete();

                    JSONArray jsonArray = new JSONArray();
                    JSONObject notiObjNew = new JSONObject();
                    try {
                        notiObjNew.put("id","0");
                        notiObjNew.put("friendRequesSenderId", currentID );
                        notiObjNew.put("message", "Your Follow Request is Approved");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    jsonArray.put(notiObjNew);
                    NotificationPublisher notificationPublisher = new NotificationPublisher(context, "FollowRequestAccept", jsonArray + "", friendRequestSenderId);
                    notificationPublisher.publishNotification();
                    onClickListner.onClick(finalPosition,null ,"");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return friendsRequest.size();
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
