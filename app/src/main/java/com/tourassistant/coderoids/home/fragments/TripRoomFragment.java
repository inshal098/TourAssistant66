package com.tourassistant.coderoids.home.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;
import com.tourassistant.coderoids.R;
import com.tourassistant.coderoids.adapters.DestinationsAdapter;
import com.tourassistant.coderoids.adapters.FriendsAdapter;
import com.tourassistant.coderoids.adapters.ImagesAdapter;
import com.tourassistant.coderoids.adapters.TripDetailAdapter;
import com.tourassistant.coderoids.adapters.TripRequestAdapter;
import com.tourassistant.coderoids.helpers.AppHelper;
import com.tourassistant.coderoids.models.PlacesModel;
import com.tourassistant.coderoids.models.Profile;
import com.tourassistant.coderoids.starttrip.StartTrip;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;


public class TripRoomFragment extends Fragment {
    RecyclerView tripDetailRv ,rvFriends;
    LinearLayout startTrip;
    LinearLayoutManager llm,fLlm;
    TextView tvTripName;
    GridView tripImages;
    private PlacesClient placesClient;
    private static final int M_MAX_ENTRIES = 10;
    private String[] likelyPlaceNames;
    private List<Place> places;
    private String[] likelyPlaceAddresses;
    private List[] likelyPlaceAttributions;
    private LatLng[] likelyPlaceLatLngs;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    TripRequestAdapter friendsAdapter;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!Places.isInitialized()) {
            Places.initialize(getContext(), getContext().getString(R.string.google_maps_key));
        }
        placesClient = Places.createClient(getActivity());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_trip_room, container, false);
        initalizeViews(view);
        return view;
    }

    private void initalizeViews(View view) {
        tripDetailRv = view.findViewById(R.id.rv_tripDetails);
        tvTripName = view.findViewById(R.id.trip_name);
        tripImages = view.findViewById(R.id.tripImages);
        rvFriends = view.findViewById(R.id.friends_list);
        startTrip = view.findViewById(R.id.start_trip_ll);

        llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);

        fLlm = new LinearLayoutManager(getActivity());
        fLlm.setOrientation(LinearLayoutManager.HORIZONTAL);
        try {
            JSONArray tripDetailArray = new JSONArray();
            tripDetailArray.put("Trip Name : " +  AppHelper.tripEntityList.getTripTitle());
            tvTripName.setText(AppHelper.tripEntityList.getTripTitle());
            tripDetailArray.put("Created By : " + AppHelper.tripEntityList.getCreatorName());
            tripDetailArray.put("Trip Destination : " +AppHelper.tripEntityList.getDestination());
            tripDetailArray.put("Starting Date : " +AppHelper.tripEntityList.getStartDate());
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
        startTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), StartTrip.class));
            }
        });
    }

    private void populateFriends(String joinTripRequests) {
        try {
            JSONArray jsonArray = new JSONArray(joinTripRequests);
            List<Profile> people = new ArrayList<>();
            for(int i=0;i<AppHelper.allUsers.size();i++){
                Profile profile = AppHelper.allUsers.get(i).toObject(Profile.class);
                for(int j=0;j<jsonArray.length();j++){
                    JSONObject jsonObject = jsonArray.getJSONObject(j);
                    String userId = jsonObject.getString("userId");
                    if(AppHelper.tripEntityList.getFirebaseUserId() != null){
                        if(profile.getUserId().matches(userId)){
                            people.add(profile);
                        }
                    }
                }
            }
            people.add(AppHelper.currentProfileInstance);
            friendsAdapter = new TripRequestAdapter(getContext(),people,null,null);
            rvFriends.setAdapter(friendsAdapter);
            rvFriends.setLayoutManager(fLlm);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void requestCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            fetchDestinationDetail(AppHelper.tripEntityList.getDestinationId());
        } else {
            // TODO: Request fine location permission
            checkLocationPermission();
            Log.d("Permission", "Request fine location permission.");
        }
    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(getActivity())
                        .setTitle("Location Permission")
                        .setMessage("Please Provide Location Permission")
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(getActivity(),
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    requestCurrentLocation();
                } else {
                }
                return;
            }

        }
    }


    private void fetchDestinationDetail(String destinationId) {
        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        rootRef.collection("Destinations").document(destinationId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                PlacesModel documentSnapshot = value.toObject(PlacesModel.class);
                final List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS,
                        Place.Field.LAT_LNG, Place.Field.ADDRESS_COMPONENTS,
                        Place.Field.BUSINESS_STATUS, Place.Field.RATING, Place.Field.PHOTO_METADATAS,
                        Place.Field.USER_RATINGS_TOTAL, Place.Field.TYPES);

                final FetchPlaceRequest request = FetchPlaceRequest.newInstance(documentSnapshot.getDestinationId(), placeFields);
                placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
                    Place place = response.getPlace();
                    AppHelper.tripRoomPlace = place;
                    populateCurrentPlaceDetail(place);
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
        });
    }

    private void populateCurrentPlaceDetail(Place place) {
        final List<PhotoMetadata> metadata = place.getPhotoMetadatas();
        ImagesAdapter imagesAdapter = new ImagesAdapter(getContext(),placesClient,metadata);
        tripImages.setAdapter(imagesAdapter);
    }

    public void findAddress(double latitude, double longitude) {
        try {
            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(getContext(), Locale.getDefault());
            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            // etAddress.setText(address);
            AppHelper.lastSearchAddress = address;
            List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS,
                    Place.Field.LAT_LNG,
                    Place.Field.BUSINESS_STATUS, Place.Field.RATING, Place.Field.PHOTO_METADATAS,
                    Place.Field.USER_RATINGS_TOTAL, Place.Field.TYPES);
//            FindCurrentPlaceRequest findCurrentPlaceRequest = FindCurrentPlaceRequest.newInstance(fields);
//            // Get the likely places - that is, the businesses and other points of interest that
//            // are the best match for the device's current location.
//            @SuppressWarnings("MissingPermission") final Task<FindCurrentPlaceResponse> placeResult =
//                    placesClient.findCurrentPlace(findCurrentPlaceRequest);
//            placeResult.addOnCompleteListener(new OnCompleteListener<FindCurrentPlaceResponse>() {
//                @Override
//                public void onComplete(@NonNull Task<FindCurrentPlaceResponse> task) {
//                    if (task.isSuccessful() && task.getResult() != null) {
//                        FindCurrentPlaceResponse likelyPlaces = task.getResult();
//
//                        // Set the count, handling cases where less than 5 entries are returned.
//                        int count;
//                        if (likelyPlaces.getPlaceLikelihoods().size() < M_MAX_ENTRIES) {
//                            count = likelyPlaces.getPlaceLikelihoods().size();
//
//                        } else {
//                            count = M_MAX_ENTRIES;
//                        }
//
//                        int i = 0;
//                        likelyPlaceNames = new String[count];
//                        likelyPlaceAddresses = new String[count];
//                        likelyPlaceAttributions = new List[count];
//                        likelyPlaceLatLngs = new LatLng[count];
//
//                        for (PlaceLikelihood placeLikelihood : likelyPlaces.getPlaceLikelihoods()) {
//                            // Build a list of likely places to show the user.
//                            places.add(placeLikelihood.getPlace());
//                            likelyPlaceNames[i] = placeLikelihood.getPlace().getName();
//                            likelyPlaceAddresses[i] = placeLikelihood.getPlace().getAddress();
//                            likelyPlaceAttributions[i] = placeLikelihood.getPlace()
//                                    .getAttributions();
//                            likelyPlaceLatLngs[i] = placeLikelihood.getPlace().getLatLng();
//                            i++;
//                            if (i > (count - 1)) {
//                                break;
//                            }
//                        }
//
//                        // Show a dialog offering the user the list of likely places, and add a
//                        // marker at the selected place.
//                        //openPlacesDialog(googleMap);
//                    } else {
//                        Log.e("Ex", "Exception: %s", task.getException());
//                    }
//                }
//            });
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}