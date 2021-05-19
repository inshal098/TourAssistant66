package com.tourassistant.coderoids.home.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.tourassistant.coderoids.BuildConfig;
import com.tourassistant.coderoids.R;
import com.tourassistant.coderoids.helpers.AppHelper;
import com.tourassistant.coderoids.helpers.NotificationPublisher;
import com.tourassistant.coderoids.home.fragments.HomeFragment;
import com.tourassistant.coderoids.plantrip.tripdb.TripEntity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TripListAdapter extends RecyclerView.Adapter<TripListAdapter.ViewHolder> {
    List<DocumentSnapshot> tripData;
    Context context;
    JSONArray tripRequestArray = new JSONArray();
    FirebaseFirestore rootRef = null;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvTripName, tvStartDate;
        public Button btnEditTrip, btnInviteFrients, btnShare, btnTripRoom;
        public MaterialButton requests;

        public ViewHolder(View view) {
            super(view);
            tvTripName = view.findViewById(R.id.tv_tripName);
            tvStartDate = view.findViewById(R.id.tv_trip_start_date);
            btnEditTrip = view.findViewById(R.id.edit_trip);
            btnInviteFrients = view.findViewById(R.id.invite_);
            btnShare = view.findViewById(R.id.share);
            requests = view.findViewById(R.id.requests);
            btnTripRoom = view.findViewById(R.id.join_trip);
        }
    }

    public TripListAdapter(FragmentActivity activity, List<DocumentSnapshot> tripData) {
        this.tripData = tripData;
        this.context = activity;
        this.rootRef = FirebaseFirestore.getInstance();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.row_trip, viewGroup, false);
        return new ViewHolder(view);
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        try {

            rootRef.collection("Trips")
                    .document(AppHelper.currentProfileInstance.getUserId())
                    .collection("UserTrips").document((tripData.get(position).getId()))
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                    TripEntity tripEntity = value.toObject(TripEntity.class);
                    if (tripEntity != null) {
                        if ((tripEntity.getJoinTripRequests() != null && !tripEntity.getJoinTripRequests().matches("null") && !tripEntity.getJoinTripRequests().matches(""))) {
                            try {
                                tripRequestArray = new JSONArray(tripEntity.getJoinTripRequests());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            viewHolder.requests.setText("Request (" + tripRequestArray.length() + ")");
                        }
                        viewHolder.tvTripName.setText("Trip Name : " + tripEntity.getTripTitle());
                        if (tripEntity.getStartDate() != null) {
                            viewHolder.tvStartDate.setText("Starting Date : " + tripEntity.getStartDate());
                        } else
                            viewHolder.tvStartDate.setText("Starting Date : -");

                        viewHolder.btnEditTrip.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AppHelper.tripEntityList = tripEntity;
                                Navigation.findNavController(v).navigate(R.id.editTripFragment);
                            }
                        });

                        viewHolder.btnInviteFrients.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AppHelper.tripEntityList = tripEntity;
                                Navigation.findNavController(v).navigate(R.id.inviteFragment);
                            }
                        });

                        viewHolder.btnShare.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent sendIntent = new Intent();
                                sendIntent.setAction(Intent.ACTION_SEND);
                                sendIntent.putExtra(Intent.EXTRA_TEXT,
                                        "Hey Checkout This New Trip im Planning ,Join Me Know" + BuildConfig.APPLICATION_ID);
                                sendIntent.setType("text/plain");
                                context.startActivity(sendIntent);
                            }
                        });
                        viewHolder.requests.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (tripEntity.getFirebaseUserId().matches(AppHelper.currentProfileInstance.getUserId())) {

                                    if (tripRequestArray.length() > 0) {
                                        AppHelper.tripEntityList = tripEntity;
                                        Navigation.findNavController(v).navigate(R.id.joinTripRequestFragment);
                                        try {
//                                        JSONArray jsonArray = new JSONArray();
//                                        JSONObject jsonObject = new JSONObject();
//                                        jsonObject.put("id" ,AppHelper.tripEntityList.getFirebaseId());
//                                        jsonObject.put("message" ,"Your Request To Join a Trip is Approved");
//                                        jsonArray.put(jsonObject);
//                                        NotificationPublisher notificationPublisher = new NotificationPublisher(context
//                                                ,"PublicTrips" ,jsonArray+"",AppHelper.tripEntityList.getFirebaseUserId());
//                                        notificationPublisher.publishNotification();
                                        } catch (Exception ex) {
                                            ex.printStackTrace();
                                        }

                                    }
                                } else {
                                    Toast.makeText(context, "Only Admin can Approve the Requests", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });


                        viewHolder.btnTripRoom.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (tripEntity.getDestination() == null || tripEntity.getDestinationId() == null) {
                                    Toast.makeText(context, "Trip Details are Not Complete", Toast.LENGTH_SHORT).show();
                                } else {
                                    AppHelper.tripEntityList = tripEntity;
                                    AppHelper.tripRoomPlace = new ArrayList<>();
                                    Navigation.findNavController(v).navigate(R.id.tripRoomFragment);
                                }
                            }
                        });

                    }
                }
            });


        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public int getItemCount() {
        return tripData.size();
    }
}
