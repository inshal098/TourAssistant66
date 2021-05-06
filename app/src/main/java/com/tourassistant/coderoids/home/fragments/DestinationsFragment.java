package com.tourassistant.coderoids.home.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.firestore.Blob;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;
import com.tourassistant.coderoids.R;
import com.tourassistant.coderoids.adapters.DestinationsAdapter;
import com.tourassistant.coderoids.adapters.PlacesAdapter;
import com.tourassistant.coderoids.helpers.AppHelper;
import com.tourassistant.coderoids.interfaces.onImageLoaded;
import com.tourassistant.coderoids.models.PlacesModel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class DestinationsFragment extends Fragment implements onImageLoaded {
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    MaterialTextView mtvAddDestination;
    RecyclerView rvPlaces, savedDestinations;
    private static int AUTOCOMPLETE_REQUEST_CODE = 1;
    PlacesClient placesClient;
    ArrayList<PlacesModel> places = new ArrayList<>();
    ArrayList<PlacesModel> fetchedPlaces = new ArrayList<>();
    ArrayList<Place> placesArray = new ArrayList<>();
    PlacesAdapter placesAdapter;
    LinearLayoutManager llm, savedDestinationLLM;
    boolean rowState[];
    MaterialButton addDestinationBtn;
    onImageLoaded onImageLoaded;
    ProgressDialog progressDialog;
    Location location;
    private final String TAG = "MapsTAG";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!Places.isInitialized()) {
            Places.initialize(getContext(), getContext().getString(R.string.google_maps_key));
        }
        placesClient = Places.createClient(getActivity());
        llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.HORIZONTAL);
        savedDestinationLLM = new LinearLayoutManager(getActivity());
        savedDestinationLLM.setOrientation(LinearLayoutManager.HORIZONTAL);
        onImageLoaded = (onImageLoaded) this;
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Please Wait...");
        progressDialog.setIndeterminate(true);
        progressDialog.show();
        fetchDestinations();
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
                    if (fetchedPlaces.size() > 0) {
                        rowState = new boolean[fetchedPlaces.size()];
                        DestinationsAdapter destinatonAdapter = new DestinationsAdapter(getActivity(), fetchedPlaces, rowState,"D");
                        savedDestinations.setAdapter(destinatonAdapter);
                        savedDestinations.setLayoutManager(savedDestinationLLM);
                    }

                }
                if (progressDialog.isShowing())
                    progressDialog.dismiss();
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_destinations, container, false);
        initializeView(view);
        return view;
    }

    private void initializeView(View view) {
        mtvAddDestination = view.findViewById(R.id.add_a_new_destination);
        rvPlaces = view.findViewById(R.id.new_destination);
        savedDestinations = view.findViewById(R.id.destinations_rv);
        addDestinationBtn = view.findViewById(R.id.add_destination_btn);
        addDestinationBtn.setVisibility(View.GONE);
        mtvAddDestination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestCurrentLocation(onImageLoaded);
            }
        });

        addDestinationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.setMessage("Adding Destination...");
                progressDialog.setIndeterminate(true);
                progressDialog.show();
                FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
                for (int i = 0; i < places.size(); i++) {
                    try {
                        DocumentReference documentReference = rootRef.collection("Destinations").document(places.get(i).getDestinationId());
                        documentReference.set(places.get(i));
                        if (progressDialog.isShowing())
                            progressDialog.dismiss();
                        fetchDestinations();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
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

    public void populateDestinations() {
        if (placesArray.size() > 0)
            addDestinationBtn.setVisibility(View.VISIBLE);
        rowState = new boolean[placesArray.size()];
        placesAdapter = new PlacesAdapter(getActivity(), placesArray, rowState, placesClient);
        rvPlaces.setAdapter(placesAdapter);
        rvPlaces.setLayoutManager(llm);
    }

    @Override
    public void imageLoadingStatus(boolean status) {
        if (status)
            populateDestinations();
        else {
            manageAndUpdatePlace(AppHelper.selectedPlace);
        }
    }

    private void requestCurrentLocation(onImageLoaded onImageLoaded) {
        // Request permission
        if (ActivityCompat.checkSelfPermission(
                getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            LocationManager locationManager = (LocationManager) getActivity().getSystemService(
                    Context.LOCATION_SERVICE);
            AppHelper.location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            MapDialogFragment fragment = new MapDialogFragment(placesClient ,onImageLoaded);
            fragment.show(getChildFragmentManager(), "");
        } else {
            // TODO: Request fine location permission
            checkLocationPermission();
            Log.d(TAG, "Request fine location permission.");
        }
    }

    public static class MapDialogFragment extends androidx.fragment.app.DialogFragment {
        Dialog customDialog;
        public GoogleMap googleMap;
        public SupportMapFragment mapFragment;
        public TextView etAddress;
        private PlacesClient placesClient;
        private static final int M_MAX_ENTRIES = 5;
        private String[] likelyPlaceNames;
        private List<Place> places;
        private String[] likelyPlaceAddresses;
        private List[] likelyPlaceAttributions;
        private LatLng[] likelyPlaceLatLngs;
        private com.tourassistant.coderoids.interfaces.onImageLoaded onImageLoaded;

        public MapDialogFragment(PlacesClient placesClient, com.tourassistant.coderoids.interfaces.onImageLoaded onImageLoaded) {
            this.placesClient = placesClient;
            this.onImageLoaded = onImageLoaded;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setStyle(STYLE_NORMAL, R.style.MY_DIALOG);
        }

        @Override
        public void onStart() {
            super.onStart();
            customDialog = getDialog();
            if (customDialog != null) {
                int width = ViewGroup.LayoutParams.MATCH_PARENT;
                int height = ViewGroup.LayoutParams.MATCH_PARENT;
                customDialog.getWindow().setLayout(width, height);
            }
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
                if (resultCode == RESULT_OK) {
                    Place place = Autocomplete.getPlaceFromIntent(data);
                    AppHelper.selectedPlace = place;
                    etAddress.setText(place.getAddress());
                    googleMap.animateCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(place.getLatLng());
                    markerOptions.title(place.getAddress());
                    googleMap.addMarker(markerOptions);
                    LatLng latLng = place.getLatLng();

                    AppHelper.lastSearchLat = latLng.latitude;
                    AppHelper.lastSearchLon = latLng.longitude;
                    AppHelper.lastSearchAddress = place.getAddress();
                    AppHelper.lastCoordinates = "" + AppHelper.lastSearchLat + "," + AppHelper.lastSearchLon;

                } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                    // TODO: Handle the error.
                    Status status = Autocomplete.getStatusFromIntent(data);
                    Log.i("Status", ((Status) status).getStatusMessage());
                } else if (resultCode == RESULT_CANCELED) {
                    // The user canceled the operation.
                }
                return;
            }
            super.onActivityResult(requestCode, resultCode, data);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View root = inflater.inflate(R.layout.dialog_map, null);
            builder.setView(root);
            etAddress = root.findViewById(R.id.et_newAddress);
            etAddress.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    manageLocation();
                }
            });
            Button btnDone = root.findViewById(R.id.btn_done);
            Button btnFind = root.findViewById(R.id.btn_find);
            Button clearField = root.findViewById(R.id.clear_all);
            clearField.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    etAddress.setText("");
                }
            });
            ImageView ivCancel = root.findViewById(R.id.ivCancel);
            initializeMap(etAddress, btnFind);
            ivCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    customDialog.dismiss();
                    AppHelper.lastSearchLat = 0;
                    AppHelper.lastSearchLon = 0;
                    AppHelper.lastSearchAddress = "";
                }
            });
            btnDone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        onImageLoaded.imageLoadingStatus(false);
                        customDialog.dismiss();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            return builder.create();
        }



        private void initializeMap(final TextView etAddress, final Button btnFind) {
            if (googleMap == null) {
                mapFragment = (SupportMapFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.map);
                mapFragment.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap mGoogleMap) {
                        googleMap = mGoogleMap;
                        populateMap(etAddress, btnFind);

                    }
                });
                Log.e("GoogleMap", "not populate");
            } else {
                Log.e("GoogleMap", "populate");
            }
        }

        private void populateMap(final TextView etAddress, final Button btnFind) {
            if (googleMap != null) {
                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                googleMap.getUiSettings().setZoomControlsEnabled(false);
                googleMap.getUiSettings().setMyLocationButtonEnabled(true);
                googleMap.getUiSettings().setCompassEnabled(true);
                googleMap.getUiSettings().setRotateGesturesEnabled(true);
                googleMap.getUiSettings().setZoomGesturesEnabled(true);
                LatLng latLng = new LatLng(AppHelper.location.getAltitude(), AppHelper.location.getLongitude());
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 11));

                final MarkerOptions marker = new MarkerOptions().position(latLng).title("Address");
                marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                googleMap.addMarker(marker);

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(AppHelper.location.getLatitude(), AppHelper.location.getLongitude())).zoom(15).build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        AppHelper.lastSearchLat = latLng.latitude;
                        AppHelper.lastSearchLon = latLng.longitude;
                        AppHelper.lastCoordinates = "" + AppHelper.lastSearchLat + "," + AppHelper.lastSearchLon;
                        findAddress(latLng, etAddress,googleMap);
                        googleMap.clear();
                        googleMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .title(AppHelper.lastSearchAddress));
                    }
                });
                btnFind.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //findAddress(etAddress, marker);
                    }
                });
            } else {
                Toast.makeText(getActivity(), "Please check your google play services", Toast.LENGTH_SHORT).show();
            }
        }

        public void findAddress(LatLng latLng, TextView etAddress, GoogleMap googleMap) {
            try {
                Geocoder geocoder;
                List<Address> addresses;
                geocoder = new Geocoder(getContext(), Locale.getDefault());
                addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                etAddress.setText(address);
                AppHelper.lastSearchAddress = address;
                List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS,
                        Place.Field.LAT_LNG,
                        Place.Field.BUSINESS_STATUS, Place.Field.RATING, Place.Field.PHOTO_METADATAS,
                        Place.Field.USER_RATINGS_TOTAL, Place.Field.TYPES);

                FindAutocompletePredictionsRequest findAutocompletePredictionsRequest = FindAutocompletePredictionsRequest.newInstance(address);
                String query = findAutocompletePredictionsRequest.getQuery();
                etAddress.setText(query);
                manageLocation();
                //FindCurrentPlaceRequest findCurrentPlaceRequest = FindCurrentPlaceRequest.newInstance(fields);
                // Get the likely places - that is, the businesses and other points of interest that
                // are the best match for the device's current location.
//                @SuppressWarnings("MissingPermission") final Task<FindCurrentPlaceResponse> placeResult =
//                        placesClient.findCurrentPlace(findCurrentPlaceRequest);
//                placeResult.addOnCompleteListener(new OnCompleteListener<FindCurrentPlaceResponse>() {
//                    @Override
//                    public void onComplete(@NonNull Task<FindCurrentPlaceResponse> task) {
//                        if (task.isSuccessful() && task.getResult() != null) {
//                            FindCurrentPlaceResponse likelyPlaces = task.getResult();
//
//                            // Set the count, handling cases where less than 5 entries are returned.
//                            int count;
//                            if (likelyPlaces.getPlaceLikelihoods().size() < M_MAX_ENTRIES) {
//                                count = likelyPlaces.getPlaceLikelihoods().size();
//
//                            } else {
//                                count = M_MAX_ENTRIES;
//                            }
//
//                            int i = 0;
//                            likelyPlaceNames = new String[count];
//                            likelyPlaceAddresses = new String[count];
//                            likelyPlaceAttributions = new List[count];
//                            likelyPlaceLatLngs = new LatLng[count];
//
//                            for (PlaceLikelihood placeLikelihood : likelyPlaces.getPlaceLikelihoods()) {
//                                if(places == null){
//                                    places = new ArrayList<>();
//                                }
//                                // Build a list of likely places to show the user.
//                                places.add(placeLikelihood.getPlace());
//                                likelyPlaceNames[i] = placeLikelihood.getPlace().getName();
//                                likelyPlaceAddresses[i] = placeLikelihood.getPlace().getAddress();
//                                likelyPlaceAttributions[i] = placeLikelihood.getPlace()
//                                        .getAttributions();
//                                likelyPlaceLatLngs[i] = placeLikelihood.getPlace().getLatLng();
//                                i++;
//                                if (i > (count - 1)) {
//                                    break;
//                                }
//                            }
//
//                            // Show a dialog offering the user the list of likely places, and add a
//                            // marker at the selected place.
//                            openPlacesDialog(googleMap);
//                        } else {
//                            Log.e("Ex", "Exception: %s", task.getException());
//                        }
//                    }
//                });
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        private void openPlacesDialog(GoogleMap googleMap) {
            // Ask the user to choose the place where they are now.
            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // The "which" argument contains the position of the selected item.
                    LatLng markerLatLng = likelyPlaceLatLngs[which];
                    String markerSnippet = likelyPlaceAddresses[which];
                    AppHelper.selectedPlace = places.get(which);
                    if (likelyPlaceAttributions[which] != null) {
                        markerSnippet = markerSnippet + "\n" + likelyPlaceAttributions[which];
                    }

                    // Add a marker for the selected place, with an info window
                    // showing information about that place.
                    googleMap.addMarker(new MarkerOptions()
                            .title(likelyPlaceNames[which])
                            .position(markerLatLng)
                            .snippet(markerSnippet));

                    // Position the map's camera at the location of the marker.
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLatLng,
                            11));

                }
            };

            // Display the dialog.
            AlertDialog dialog = new AlertDialog.Builder(getContext())
                    .setTitle("Pick a Place")
                    .setItems(likelyPlaceNames, listener)
                    .show();
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            SupportMapFragment f = (SupportMapFragment) getActivity().getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            if (f != null)
                getActivity().getSupportFragmentManager().beginTransaction().remove(f).commit();
        }

        private void manageLocation() {
            List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS,
                    Place.Field.LAT_LNG, Place.Field.ADDRESS_COMPONENTS,
                    Place.Field.BUSINESS_STATUS, Place.Field.RATING, Place.Field.PHOTO_METADATAS,
                    Place.Field.USER_RATINGS_TOTAL, Place.Field.TYPES);
            List<String> countriesArr = new ArrayList<>();
            countriesArr.add("PK");
            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                    .setCountries(countriesArr)
                    .build(getActivity());
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
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
                    requestCurrentLocation(onImageLoaded);

                } else {
                }
                return;
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
            onImageLoaded.imageLoadingStatus(true);
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
                places.add(placesModel);
            }).addOnFailureListener((exception) -> {
                if (exception instanceof ApiException) {
                    final ApiException apiException = (ApiException) exception;
                    final int statusCode = apiException.getStatusCode();
                    // TODO: Handle error with given status code.
                }
            });
        } else {
            Toast.makeText(getContext(), "Place is Already added in Destinations", Toast.LENGTH_SHORT).show();
        }
    }




}