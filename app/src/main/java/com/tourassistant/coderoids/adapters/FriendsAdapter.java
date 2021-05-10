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
import com.tourassistant.coderoids.helpers.NotificationPublisher;
import com.tourassistant.coderoids.interfaces.onClickListner;
import com.tourassistant.coderoids.models.FriendRequestModel;
import com.tourassistant.coderoids.plantrip.tripdb.TripEntity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.ViewHolder> {
    Context context;
    List<DocumentSnapshot> friendsList;
    boolean[] rowState;
    com.tourassistant.coderoids.interfaces.onClickListner onClickListner;
    JSONArray tripRequest;

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
                String currentUser = documentSnapshot.getString("userFirestoreIdReceiver");
                String reciverId = documentSnapshot.getString("userFirestoreIdSender");

                if(currentUser.matches(AppHelper.currentProfileInstance.getUserId())) {
                    currentUser = AppHelper.currentProfileInstance.getUserId();
                }
                else if(currentUser.matches(AppHelper.currentProfileInstance.getUserId())) {
                    currentUser =documentSnapshot.getString("userFirestoreIdSender");
                    reciverId =documentSnapshot.getString("userFirestoreIdReceiver");
                }

                viewHolder.mtUserName.setText(documentSnapshot.getString("userName"));
                if (!AppHelper.tripEntityList.getJoinTripRequests().matches("") && AppHelper.tripEntityList.getJoinTripRequests() != null) {
                    tripRequest = new JSONArray(AppHelper.tripEntityList.getJoinTripRequests());
                    for (int i = 0; i < tripRequest.length(); i++) {
                        if (tripRequest.getJSONObject(i).getString("userId").matches(reciverId) && tripRequest.getJSONObject(i).has("status") && tripRequest.getJSONObject(i).getString("status").matches("1")) {
                            rowState[position] = true;
                            break;
                        } else if (tripRequest.getJSONObject(i).getString("userId").matches(reciverId) && !tripRequest.getJSONObject(i).has("status")) {
                            rowState[position] = false;
                            break;
                        }
                    }

                }
                if (rowState != null) {
                    if (rowState[position]) {
                        viewHolder.btnFollow.setText("Added");
                        viewHolder.btnFollow.setBackgroundColor(context.getResources().getColor(R.color.green));
                    } else
                        viewHolder.btnFollow.setText("Add");
                } else {
                    viewHolder.btnFollow.setVisibility(View.GONE);
                }
                int finalPosition = position;
                String finalReciverId = reciverId;
                String finalCurrentUser = currentUser;
                viewHolder.btnFollow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (viewHolder.btnFollow.getText().toString().matches("Add")) {
                            if (!rowState[finalPosition]) {
                                rowState[finalPosition] = true;
                                FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
                                if (tripRequest == null || tripRequest.length() == 0)
                                    tripRequest = new JSONArray();
                                JSONObject jsonObject = new JSONObject();
                                try {
                                    jsonObject.put("userId", finalReciverId);
                                    jsonObject.put("status", "1");

                                    tripRequest.put(jsonObject);
                                    DocumentReference uidRefPublic = rootRef.collection("PublicTrips").document(AppHelper.tripEntityList.getFirebaseId());
                                    DocumentReference uidRefPublicPers = rootRef.collection("Trips").document(AppHelper.currentProfileInstance.getUserId()).collection("UserTrips").document(AppHelper.tripEntityList.getFirebaseId());
                                    uidRefPublicPers.update("joinTripRequests", tripRequest + "");
                                    uidRefPublic.update("joinTripRequests", tripRequest + "");
                                    JSONArray jsonArray = new JSONArray();
                                    JSONObject notiObj = new JSONObject();
                                    notiObj.put("id", AppHelper.tripEntityList.getFirebaseId());
                                    notiObj.put("message", "You Have been Added to a Trip");
                                    jsonArray.put(jsonObject);
                                    NotificationPublisher notificationPublisher = new NotificationPublisher(context, "PublicTripsRequest", jsonArray + "", finalReciverId);
                                    notificationPublisher.publishNotification();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } else
                                Toast.makeText(context, "Invitation To this User is Already Sent", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
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
