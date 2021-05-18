package com.tourassistant.coderoids.starttrip;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.Blob;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;
import com.tourassistant.coderoids.R;
import com.tourassistant.coderoids.adapters.DestinationsAdapter;
import com.tourassistant.coderoids.adapters.DestinationsListEditAdater;
import com.tourassistant.coderoids.helpers.AppHelper;
import com.tourassistant.coderoids.helpers.NotificationPublisher;
import com.tourassistant.coderoids.models.PlacesModel;
import com.tourassistant.coderoids.plantrip.tripdb.TripEntity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class ReplanTrip extends AppCompatActivity {
    RecyclerView rvDestinationEdit;
    LinearLayoutManager llm;
    TextView tvGeoPoint;
    Button btnSave;
    public static ReplanTrip instance;
    Button btnAddDestination;
    DestinationsListEditAdater tripsDestinationAdapter;
    JSONArray destArr;
    private int AUTOCOMPLETE_REQUEST_CODE = 3;
    GeoPoint point;
    int positionToUpdate = -1;
    private PlacesClient placesClient;
    private static final int M_MAX_ENTRIES = 10;
    private String[] likelyPlaceNames;
    private List<Place> places = new ArrayList<>();
    private String[] likelyPlaceAddresses;
    private List[] likelyPlaceAttributions;
    private LatLng[] likelyPlaceLatLngs;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    TextInputEditText tripTitle;
    ArrayList<Place> placesArray = new ArrayList<>();
    ArrayList<PlacesModel> placesModeNew = new ArrayList<>();
    ArrayList<PlacesModel> fetchedPlaces = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_replan_trip);
        instance = this;
        placesClient = Places.createClient(this);

        //rvDestinationsCurrent = findViewById(R.id.rv_destinations);
        rvDestinationEdit = findViewById(R.id.destinations_list_edit);
        btnAddDestination = findViewById(R.id.button);
        tvGeoPoint = findViewById(R.id.tl_geo_point);
        tripTitle = findViewById(R.id.et_trip_title);
        btnSave = findViewById(R.id.button_save);
        llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        try {
            if (AppHelper.tripEntityList != null) {
                TripEntity tripEntity = AppHelper.tripEntityList;
                if (tripEntity.getDestinationId() != null && !tripEntity.getDestinationId().matches("")) {
                    destArr = new JSONArray(tripEntity.getDestinationId());
                    tripsDestinationAdapter = new DestinationsListEditAdater(this, destArr);
                    rvDestinationEdit.setAdapter(tripsDestinationAdapter);
                    rvDestinationEdit.setLayoutManager(llm);
                }
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
            FirebaseCrashlytics.getInstance().recordException(ex);
        }


        btnAddDestination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addDestinationToList(0, "NEW");
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addDestinationToList(0, "Place");
            }
        });
        tripTitle.setText(AppHelper.tripEntityList.getTripTitle());
    }

    public void addDestinationToList(int pos, String type) {
        if (type.matches("Place")) {
            updateDestinationInDB();
            FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
            AppHelper.tripEntityList.setDestinationId(destArr + "");
            if(placesModeNew != null && placesModeNew.size()>0){
                String destinationName =  "";
                for(int i=0; i<placesModeNew.size();i++){
                    if(destinationName.matches("")){
                        destinationName = placesModeNew.get(i).getDestinationName();
                    } else {
                        destinationName = destinationName +"," + placesModeNew.get(i).getDestinationName();
                    }
                }
                AppHelper.tripEntityList.setDestination(destinationName);
            }
            AppHelper.tripEntityList.setTripTitle(tripTitle.getText().toString());
            rootRef.collection("PublicTrips").document(AppHelper.tripEntityList.getFirebaseId()).set(AppHelper.tripEntityList);
            rootRef.collection("Trips")
                    .document(AppHelper.currentProfileInstance.getUserId())
                    .collection("UserTrips")
                    .document(AppHelper.tripEntityList.getFirebaseId())
                    .set(AppHelper.tripEntityList).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        try {
                            JSONArray jsonArray = new JSONArray(AppHelper.tripEntityList.getJoinTripRequests());

                            if(jsonArray!= null && jsonArray.length()>0){
                                for(int i=0;i<jsonArray.length();i++){
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    String tripMembers = jsonObject.getString("userId");
                                    JSONArray jsonArray1 = new JSONArray();
                                    JSONObject notiObjNew = new JSONObject();
                                    try {
                                        notiObjNew.put("id",AppHelper.tripEntityList.getFirebaseId());
                                        notiObjNew.put("message", "A Trip is Replanned");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    jsonArray1.put(notiObjNew);
                                    NotificationPublisher notificationPublisher = new NotificationPublisher(ReplanTrip.this, "TripReplanned", jsonArray1 + "", tripMembers);
                                    notificationPublisher.publishNotification();
                                }
                            }
                        } catch (JSONException ex) {
                            ex.printStackTrace();
                            FirebaseCrashlytics.getInstance().recordException(ex);
                        }
                    }
                }
            });

        } else {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("destName", "Add New Place");
                jsonObject.put("destId", "");
                destArr.put(jsonObject);
                tripsDestinationAdapter = new DestinationsListEditAdater(this, destArr);
                rvDestinationEdit.setAdapter(tripsDestinationAdapter);
                rvDestinationEdit.setLayoutManager(llm);
            } catch (JSONException ex) {
                ex.printStackTrace();
                FirebaseCrashlytics.getInstance().recordException(ex);
            }

        }
    }

    public void removeDestination(int pos) {
        destArr.remove(pos);
        tripsDestinationAdapter.notifyDataSetChanged();
    }

    public void findNewDestination(int finalPosition) {
        positionToUpdate = finalPosition;
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS,
                Place.Field.LAT_LNG, Place.Field.ADDRESS_COMPONENTS,
                Place.Field.BUSINESS_STATUS, Place.Field.RATING, Place.Field.PHOTO_METADATAS,
                Place.Field.USER_RATINGS_TOTAL, Place.Field.TYPES);
        List<String> countriesArr = new ArrayList<>();
        countriesArr.add("PK");
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .setCountries(countriesArr)
                .build(this);
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                point = new GeoPoint(place.getLatLng().latitude, place.getLatLng().longitude);
                try {
                    destArr.getJSONObject(positionToUpdate).put("positionLat", place.getLatLng().latitude + "");
                    destArr.getJSONObject(positionToUpdate).put("positionLong", place.getLatLng().longitude + "");
                    destArr.getJSONObject(positionToUpdate).put("address", place.getAddress());
                    destArr.getJSONObject(positionToUpdate).put("destName", place.getAddress());
                    destArr.getJSONObject(positionToUpdate).put("destId", place.getId());
                    tripsDestinationAdapter.notifyDataSetChanged();
                    positionToUpdate = -1;
                    manageAndUpdatePlace(place);
                } catch (JSONException e) {
                    e.printStackTrace();
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
            }
        }
    }

    public void findAddress(int pos) {
        try {
            positionToUpdate = pos;
            List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS,
                    Place.Field.LAT_LNG,
                    Place.Field.BUSINESS_STATUS, Place.Field.RATING, Place.Field.PHOTO_METADATAS,
                    Place.Field.USER_RATINGS_TOTAL, Place.Field.TYPES);
            FindCurrentPlaceRequest findCurrentPlaceRequest = FindCurrentPlaceRequest.newInstance(fields);
            @SuppressWarnings("MissingPermission") final Task<FindCurrentPlaceResponse> placeResult =
                    placesClient.findCurrentPlace(findCurrentPlaceRequest);
            placeResult.addOnCompleteListener(new OnCompleteListener<FindCurrentPlaceResponse>() {
                @Override
                public void onComplete(@NonNull Task<FindCurrentPlaceResponse> task) {
                    if (task.isSuccessful() && task.getResult() != null) {
                        FindCurrentPlaceResponse likelyPlaces = task.getResult();
                        int count;
                        if (likelyPlaces.getPlaceLikelihoods().size() < M_MAX_ENTRIES) {
                            count = likelyPlaces.getPlaceLikelihoods().size();

                        } else {
                            count = M_MAX_ENTRIES;
                        }
                        int i = 0;
                        likelyPlaceNames = new String[count];
                        likelyPlaceAddresses = new String[count];
                        likelyPlaceAttributions = new List[count];
                        likelyPlaceLatLngs = new LatLng[count];

                        for (PlaceLikelihood placeLikelihood : likelyPlaces.getPlaceLikelihoods()) {
                            // Build a list of likely places to show the user.
                            places.add(placeLikelihood.getPlace());
                            likelyPlaceNames[i] = placeLikelihood.getPlace().getName();
                            likelyPlaceAddresses[i] = placeLikelihood.getPlace().getAddress();
                            likelyPlaceAttributions[i] = placeLikelihood.getPlace()
                                    .getAttributions();
                            likelyPlaceLatLngs[i] = placeLikelihood.getPlace().getLatLng();
                            i++;
                            if (i > (count - 1)) {
                                break;
                            }
                        }
                        openPlacesDialog();
                    } else {
                        Log.e("Ex", "Exception: %s", task.getException());
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    private void openPlacesDialog() {
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    Place placed = places.get(which);
                    destArr.getJSONObject(positionToUpdate).put("positionLat", placed.getLatLng().latitude + "");
                    destArr.getJSONObject(positionToUpdate).put("positionLong", placed.getLatLng().longitude + "");
                    destArr.getJSONObject(positionToUpdate).put("address", placed.getAddress());
                    destArr.getJSONObject(positionToUpdate).put("destName", placed.getAddress());
                    destArr.getJSONObject(positionToUpdate).put("destId", placed.getId());
                    tripsDestinationAdapter.notifyDataSetChanged();
                    positionToUpdate = -1;
                    manageAndUpdatePlace(placed);
                } catch (JSONException ex) {
                    ex.printStackTrace();
                    FirebaseCrashlytics.getInstance().recordException(ex);
                }
            }
        };
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Pick a Place")
                .setItems(likelyPlaceNames, listener)
                .show();
    }

    public void updateDestinationInDB(){
        if(placesModeNew != null)
        for (int i = 0; i < placesModeNew.size(); i++) {
            try {
                FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
                DocumentReference documentReference = rootRef.collection("Destinations").document(placesModeNew.get(i).getDestinationId());
                documentReference.set(placesModeNew.get(i));
                fetchDestinations();
            } catch (Exception ex) {
                ex.printStackTrace();
                FirebaseCrashlytics.getInstance().recordException(ex);
            }
        }
    }

    public void manageAndUpdatePlace(Place placeData){
        Place place = placeData;
        if (!validatePlace(place)) {
            if (placesArray == null)
                placesArray = new ArrayList<>();
            placesArray.add(place);
            PlacesModel placesModel = new PlacesModel();
            placesModel.setDestinationId(place.getId());
            placesModel.setDestinationName(place.getName());
            if (place.getRating() != null)
                placesModel.setDestinationRating(place.getRating() + "");
            else
                placesModel.setDestinationRating("0.0");
            Place.BusinessStatus bussenessStatus = place.getBusinessStatus();
            if (bussenessStatus != null)
                placesModel.setDestinationStatus(bussenessStatus + "");
            else
                placesModel.setDestinationStatus("No Status Found");

            placesModel.setDestinationAddress(place.getAddress());
            GeoPoint point = new GeoPoint(place.getLatLng().latitude, place.getLatLng().longitude);
            placesModel.setDestinationCoordinates(point);
            //placesModel.setBitmaps(bitmapArray);
            final PhotoMetadata photoMetadata = place.getPhotoMetadatas().get(0);
            final String attributions = photoMetadata.getAttributions();
            final FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
                    .setMaxWidth(500) // Optional.
                    .setMaxHeight(300) // Optional.
                    .build();
            placesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
                Bitmap bmp = fetchPhotoResponse.getBitmap();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                Blob blob = Blob.fromBytes(byteArray);
                placesModel.setBlob(blob);
                placesModeNew.add(placesModel);
            }).addOnFailureListener((exception) -> {
                if (exception instanceof ApiException) {
                    final ApiException apiException = (ApiException) exception;
                    final int statusCode = apiException.getStatusCode();
                    FirebaseCrashlytics.getInstance().recordException(exception);

                    // TODO: Handle error with given status code.
                }
            });
        } else {
            Toast.makeText(this, "Place is Already added in Destinations", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validatePlace(Place place) {
        if (fetchedPlaces != null) {
            for (int i = 0; i < fetchedPlaces.size(); i++) {
                if (fetchedPlaces.get(i).getDestinationId().matches(place.getId())) {
                    return true;
                }
            }
        } else {
            return false;
        }
        return false;
    }

    private void fetchDestinations() {
        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        rootRef.collection("Destinations").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isComplete()) {
                    List<DocumentSnapshot> placesNew = task.getResult().getDocuments();
                    fetchedPlaces = new ArrayList<>();
                    for (int i = 0; i < placesNew.size(); i++) {
                        DocumentSnapshot documentSnapshot = placesNew.get(i);
                        fetchedPlaces.add(documentSnapshot.toObject(PlacesModel.class));
                    }
                }
            }
        });

    }
}