package com.tourassistant.coderoids.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.tourassistant.coderoids.R;
import com.tourassistant.coderoids.helpers.AppHelper;
import com.tourassistant.coderoids.helpers.NotificationPublisher;
import com.tourassistant.coderoids.interfaces.onClickListner;
import com.tourassistant.coderoids.plantrip.tripdb.TripEntity;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PublicTripsAdapter extends RecyclerView.Adapter<PublicTripsAdapter.ViewHolder> {
    Activity context;
    List<DocumentSnapshot> publicTrips;
    onClickListner onClickListner;

    public PublicTripsAdapter(Activity context, List<DocumentSnapshot> publicTrips, onClickListner onClickListner) {
        this.context = context;
        this.publicTrips = publicTrips;
        this.onClickListner = onClickListner;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.row_public_trips, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int position) {
        position = viewHolder.getAdapterPosition();
        try {
            DocumentSnapshot documentSnapshot = publicTrips.get(position);
            viewHolder.tvCreatedBy.setText("By: " + documentSnapshot.getString("creatorName"));
            viewHolder.tvTripName.setText("Destination: " + documentSnapshot.getString("destination"));
            viewHolder.startDate.setText("Start Date: " + documentSnapshot.getString("startDate"));
            int finalPosition = position;
            String joinTripRequests = documentSnapshot.getString("joinTripRequests");
            if(joinTripRequests != null && !joinTripRequests.matches("null") && !joinTripRequests.matches("")){
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                JSONArray jsonArray = new JSONArray(joinTripRequests);
                JSONObject jsonObject = new JSONObject();
                boolean isMatched = false;
                for (int i = 0; i < jsonArray.length(); i++) {
                    jsonObject = jsonArray.getJSONObject(i);
                    if (jsonObject.getString("userId").matches(firebaseUser.getUid())) {
                        isMatched = true;
                        break;
                    }
                }
                if(isMatched) {
                    if (jsonObject.has("status") && jsonObject.getString("status").matches("1")) {
                        viewHolder.trips.setText("Trip Room");
                        viewHolder.trips.setBackgroundColor(context.getColor(R.color.dark_grey_alpha));
                    } else {
                        viewHolder.trips.setText("Request Sent");
                        viewHolder.trips.setBackgroundColor(context.getColor(R.color.green));
                    }
                } else {
                    viewHolder.trips.setText("Join Trip");
                    viewHolder.trips.setBackgroundColor(context.getColor(R.color.purple));
                }
            }

            viewHolder.trips.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (viewHolder.trips.getText().toString().matches("Join Trip")) {
                        viewHolder.trips.setText("Request Sent");
                        viewHolder.trips.setBackgroundColor(context.getColor(R.color.green));
                        DocumentSnapshot documentSnapshot1 = documentSnapshot;
                        onClickListner.onClick(finalPosition, documentSnapshot1);
                        try {
                            JSONArray jsonArray = new JSONArray();
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("id" ,documentSnapshot.getId());
                            jsonObject.put("message" ,AppHelper.currentProfileInstance.getDisplayName()+" has Requested To Join a Trip");
                            jsonArray.put(jsonObject);
                            NotificationPublisher notificationPublisher = new NotificationPublisher(context
                                    ,"PublicTripsRequest" ,jsonArray+"",documentSnapshot.getString("firebaseUserId"));
                            notificationPublisher.publishNotification();
                        }catch (JSONException ex){
                            ex.printStackTrace();
                        }
                    } else if (viewHolder.trips.getText().toString().matches("Trip Room")) {
                        AppHelper.tripRoomSnap = documentSnapshot;
                        AppHelper.tripRoomPlace = new ArrayList<>();
                        AppHelper.tripEntityList  = documentSnapshot.toObject(TripEntity.class);
                        Navigation.findNavController(v).navigate(R.id.tripRoomFragment);
                    } else if (viewHolder.trips.getText().toString().matches("Request Sent")) {
                        Toast.makeText(context, "Pending Approval From Admin", Toast.LENGTH_SHORT).show();
                    }
                    //
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return publicTrips.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCreatedBy, tvTripName, startDate;
        Button trips;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTripName = itemView.findViewById(R.id.tripName);
            tvCreatedBy = itemView.findViewById(R.id.trip_created);
            startDate = itemView.findViewById(R.id.start_date);
            trips = itemView.findViewById(R.id.join_trip);
        }
    }
}

