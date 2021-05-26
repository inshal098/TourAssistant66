package com.tourassistant.coderoids.home.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tourassistant.coderoids.R;
import com.tourassistant.coderoids.adapters.FollowRequestAdapter;
import com.tourassistant.coderoids.adapters.TripRequestAdapter;
import com.tourassistant.coderoids.api.NotificationAPI;
import com.tourassistant.coderoids.helpers.AppHelper;
import com.tourassistant.coderoids.helpers.NotificationPublisher;
import com.tourassistant.coderoids.interfaces.onClickListner;
import com.tourassistant.coderoids.models.Profile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class JoinTripRequestFragment extends Fragment implements onClickListner {
    TripRequestAdapter tripRequestAdapter;
    RecyclerView rvJoinTrip;
    LinearLayoutManager llm;
    int time = 10000;
    onClickListner onClickListner;
    List<Profile> profiles;
    JSONArray tripRequest;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onClickListner = (onClickListner) this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_join_trip_request, container, false);
        initializeView(view);
        return view;
    }

    private void initializeView(View view) {
        getUsersProfile();
        rvJoinTrip = view.findViewById(R.id.rv_join_trip_request);
        llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.HORIZONTAL);
    }

    private void getUsersProfile() {
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Please Wait While We Load your Content...");
        progressDialog.setIndeterminate(true);
        progressDialog.show();
        try {
            tripRequest = new JSONArray(AppHelper.tripEntityList.getJoinTripRequests());
            if (tripRequest.length() > 6) {
                time = 15000;
            }
            profiles = new ArrayList<>();
            for (int i = 0; i < tripRequest.length(); i++) {
                JSONObject jsonObject = tripRequest.getJSONObject(i);
                String userId = jsonObject.getString("userId");
                FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
                rootRef.collection("Users").document(userId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        try {
                            Profile userProfile = documentSnapshot.toObject(Profile.class);
                            profiles.add(userProfile);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });
            }
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    if (profiles.size() > 0) {
                        tripRequestAdapter = new TripRequestAdapter(getContext(), profiles, onClickListner,tripRequest);
                        rvJoinTrip.setAdapter(tripRequestAdapter);
                        rvJoinTrip.setLayoutManager(llm);
                    }
                    progressDialog.dismiss();
                }
            }, 10000);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(int pos, DocumentSnapshot documentSnapshot , String tag) {
        try {
            FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
            String tripId = AppHelper.tripEntityList.getFirebaseId();
            String userId = AppHelper.currentProfileInstance.getUserId();
            DocumentReference uidRefPublic = rootRef.collection("PublicTrips").document(tripId);
            Profile profile = profiles.get(pos);
            String recieverId =  profile.getUserId();
            if (tripRequest.toString().contains(profile.getUserId())) {
                for (int i = 0; i < tripRequest.length(); i++) {
                    if (tripRequest.getJSONObject(i).getString("userId").matches(profile.getUserId())) {
                        tripRequest.getJSONObject(i).put("status", "1");

                    }
                }
            }
            uidRefPublic.update("joinTripRequests", tripRequest + "");
            DocumentReference uidRefPersonal = rootRef.collection("Trips").document(userId).collection("UserTrips").document(AppHelper.tripEntityList.getFirebaseId());
            uidRefPersonal.update("joinTripRequests", tripRequest + "");

            JSONArray jsonArray = new JSONArray();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id" ,tripId);
            jsonObject.put("message" ,"Your Request To Join a Trip is Approved");
            jsonArray.put(jsonObject);
            NotificationPublisher notificationPublisher = new NotificationPublisher(getContext(),"PublicTripsRequest" ,jsonArray+"",profile.getUserId());
            notificationPublisher.publishNotification();
            profiles.remove(pos);
            tripRequestAdapter.notifyDataSetChanged();
        } catch (JSONException ex) {
            ex.printStackTrace();
        }

    }
}