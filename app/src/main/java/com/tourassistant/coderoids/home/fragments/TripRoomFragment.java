package com.tourassistant.coderoids.home.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.model.LatLng;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import com.tourassistant.coderoids.R;
import com.tourassistant.coderoids.adapters.DestinationImagesAdapter;
import com.tourassistant.coderoids.adapters.TripDetailAdapter;
import com.tourassistant.coderoids.adapters.TripRequestAdapter;
import com.tourassistant.coderoids.chatmodule.ChatParentActivity;
import com.tourassistant.coderoids.helpers.AppHelper;
import com.tourassistant.coderoids.helpers.NotificationPublisher;
import com.tourassistant.coderoids.models.PlacesModel;
import com.tourassistant.coderoids.models.Profile;
import com.tourassistant.coderoids.plantrip.tripdb.TripEntity;
import com.tourassistant.coderoids.starttrip.StartTrip;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;


public class TripRoomFragment extends Fragment {
    RecyclerView tripDetailRv, rvFriends;
    LinearLayout startTrip;
    LinearLayoutManager llm, fLlm;
    TextView tvTripName, startTripTag;
    RecyclerView tripImages;
    LinearLayoutManager linearLayoutManager;
    private PlacesClient placesClient;
    private static final int M_MAX_ENTRIES = 10;
    private String[] likelyPlaceNames;
    private List<Place> places;
    private String[] likelyPlaceAddresses;
    private List[] likelyPlaceAttributions;
    private LatLng[] likelyPlaceLatLngs;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    TripRequestAdapter friendsAdapter;
    LinearLayout chat;
    DestinationImagesAdapter imagesAdapter;
    List<List<PhotoMetadata>> metadata;
    String destinationStringArrNames[];
    public static TripRoomFragment instance;
    SharedPreferences.Editor editorLogin;
    SharedPreferences prefLogin;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!Places.isInitialized()) {
            Places.initialize(getContext(), getContext().getString(R.string.google_maps_key));
        }
        placesClient = Places.createClient(getActivity());
        instance = this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_trip_room, container, false);
        initalizeViews(view);
        return view;
    }

    public void updateTripUI(String tripId){
        if(tripId.matches(AppHelper.tripEntityList.getFirebaseId())){
            FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
            rootRef.collection("PublicTrips").document(tripId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                    if (value != null) {
                        AppHelper.tripEntityList = value.toObject(TripEntity.class);
                        manageTripUI();
                    }
                }
            });
        } else {
            Toast.makeText(getContext(), "You Have an Update of a trip you are a member of, Go Back to Home and enter the Room", Toast.LENGTH_SHORT).show();
        }

    }

    private void initalizeViews(View view) {
        tripDetailRv = view.findViewById(R.id.rv_tripDetails);
        tvTripName = view.findViewById(R.id.trip_name);
        startTripTag = view.findViewById(R.id.startTrip_tag);
        tripImages = view.findViewById(R.id.tripImages);
        rvFriends = view.findViewById(R.id.friends_list);
        startTrip = view.findViewById(R.id.start_trip_ll);
        chat = view.findViewById(R.id.chat);
        linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        prefLogin = getContext().getSharedPreferences("logindata", Context.MODE_PRIVATE);
        editorLogin = prefLogin.edit();
        llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);

        fLlm = new LinearLayoutManager(getActivity());
        fLlm.setOrientation(LinearLayoutManager.HORIZONTAL);
        manageTripUI();
        startTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (AppHelper.tripEntityList.getFirebaseUserId().matches(AppHelper.currentProfileInstance.getUserId())) {
                        JSONArray jsonArray = new JSONArray();
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("id", AppHelper.tripEntityList.getFirebaseUserId());
                        try {
                            jsonObject.put("message", "Admin Have Started a Trip which you are a member of");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        jsonArray.put(jsonObject);
                        if(AppHelper.tripEntityList.getJoinTripRequests() != null && !AppHelper.tripEntityList.getJoinTripRequests().matches("")) {
                            JSONArray tripRequestsOrMembers = new JSONArray(AppHelper.tripEntityList.getJoinTripRequests());
                            for(int i=0;i<tripRequestsOrMembers.length();i++) {
                                JSONObject tripRequestsOrMembersJSONObject = tripRequestsOrMembers.getJSONObject(i);
                                String tripMembers = tripRequestsOrMembersJSONObject.getString("userId");
                                NotificationPublisher notificationPublisher = new NotificationPublisher(getContext(), "TripInProgress", jsonArray + "",tripMembers);
                                notificationPublisher.publishNotification();
                            }
                        }
                        startActivity(new Intent(getActivity(), StartTrip.class));
                    } else {
                        if(AppHelper.tripEntityList.getTripLocationTracking().matches("1")){
                            startActivity(new Intent(getActivity(), StartTrip.class));

                        } else
                        Toast.makeText(getContext(), "Trip is Not in Progress Currently,Only Admin Can Start The Trip..", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
            }
        });

        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), ChatParentActivity.class));
            }
        });
    }

    private void manageTripUI() {
        try {
            JSONArray tripDetailArray = new JSONArray();
            tripDetailArray.put("Trip Name : " + AppHelper.tripEntityList.getTripTitle());
            tvTripName.setText(AppHelper.tripEntityList.getTripTitle());
            tripDetailArray.put("Created By : " + AppHelper.tripEntityList.getCreatorName());
            tripDetailArray.put("Trip Destination : " + AppHelper.tripEntityList.getDestination());
            tripDetailArray.put("Starting Date : " + AppHelper.tripEntityList.getStartDate());
            TripDetailAdapter tripDetailAdapter = new TripDetailAdapter(getContext(), tripDetailArray);
            tripDetailRv.setAdapter(tripDetailAdapter);
            tripDetailRv.setLayoutManager(llm);
            if (AppHelper.tripEntityList.getDestinationId() != null && !AppHelper.tripEntityList.getDestinationId().matches("")) {
                requestCurrentLocation();
                fetchDestinationDetail(AppHelper.tripEntityList.getDestinationId());
            }
            populateFriends(AppHelper.tripEntityList.getJoinTripRequests());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (prefLogin.getString("isTripInProgress", "0").matches("1") || AppHelper.tripEntityList.getTripLocationTracking().matches("1")) {
            if(AppHelper.tripEntityList.getTripLocationTracking().matches("1") && !AppHelper.tripEntityList.getFirebaseUserId().matches(AppHelper.currentProfileInstance.getUserId())){
                startTripTag.setText("Enter Trip Now");
            } else
                startTripTag.setText("Continue");
        }
    }

    private void populateFriends(String joinTripRequests) {
        try {
            JSONArray tripRequestsOrMembers = new JSONArray(joinTripRequests);
            boolean adminAdded = false;
            List<Profile> people = new ArrayList<>();
            for (int i = 0; i < AppHelper.allUsers.size(); i++) {
                Profile profile = AppHelper.allUsers.get(i).toObject(Profile.class);
                if (AppHelper.tripEntityList.getFirebaseUserId() != null) {
                    if (profile.getUserId().matches(AppHelper.tripEntityList.getFirebaseUserId()) && !adminAdded) {
                        adminAdded = true;
                        people.add(profile);
                    }
                }
                for (int j = 0; j < tripRequestsOrMembers.length(); j++) {
                    JSONObject tripRequestsOrMembersJSONObject = tripRequestsOrMembers.getJSONObject(j);
                    String tripMembers = tripRequestsOrMembersJSONObject.getString("userId");
                    if (profile.getUserId().matches(tripMembers)) {
                        people.add(profile);
                    }

                }
            }
            if (!AppHelper.currentProfileInstance.getUserId().matches(AppHelper.tripEntityList.getFirebaseUserId()) || !adminAdded)
                people.add(AppHelper.currentProfileInstance);
            friendsAdapter = new TripRequestAdapter(getContext(), people, null, null);
            rvFriends.setAdapter(friendsAdapter);
            rvFriends.setLayoutManager(fLlm);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void requestCurrentLocation() {
        fetchDestinationDetail(AppHelper.tripEntityList.getDestinationId());
    }

    private void fetchDestinationDetail(String destinationId) {
        try {
            JSONArray jsonArray = new JSONArray(destinationId);
            FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                rootRef.collection("Destinations").document(jsonObject.getString("destId")).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                        PlacesModel documentSnapshot = value.toObject(PlacesModel.class);
                        final List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS,
                                Place.Field.LAT_LNG, Place.Field.ADDRESS_COMPONENTS,
                                Place.Field.BUSINESS_STATUS, Place.Field.RATING, Place.Field.PHOTO_METADATAS,
                                Place.Field.USER_RATINGS_TOTAL, Place.Field.TYPES);
                        if (documentSnapshot.getDestinationId() != null) {
                            final FetchPlaceRequest request = FetchPlaceRequest.newInstance(documentSnapshot.getDestinationId(), placeFields);
                            placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
                                Place place = response.getPlace();
                                if (AppHelper.tripRoomPlace == null) {
                                    AppHelper.tripRoomPlace = new ArrayList<>();
                                }
                                AppHelper.tripRoomPlace.add(place);
                                if (metadata == null)
                                    metadata = new ArrayList<>();
                                metadata.add(place.getPhotoMetadatas());
                                populateCurrentPlaceDetail(jsonArray);
                                Log.i("Status", "Place found: " + place.getName());
                            }).addOnFailureListener((exception) -> {
                                if (exception instanceof ApiException) {
                                    final ApiException apiException = (ApiException) exception;
                                    Log.e("Status", "Place not found: " + exception.getMessage());
                                    final int statusCode = apiException.getStatusCode();
                                    // TODO: Handle error with given status code.
                                }
                            });
                        }
                    }
                });
            }
        }catch (JSONException ex){

        }

    }

    private void populateCurrentPlaceDetail(JSONArray jsonArray) {
        if (imagesAdapter == null) {
            imagesAdapter = new DestinationImagesAdapter(getContext(), placesClient, metadata, jsonArray);
            tripImages.setAdapter(imagesAdapter);
            tripImages.setLayoutManager(linearLayoutManager);
        } else
            imagesAdapter.notifyDataSetChanged();
    }
}