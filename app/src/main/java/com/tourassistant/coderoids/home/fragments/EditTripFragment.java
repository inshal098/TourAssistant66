package com.tourassistant.coderoids.home.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Address;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.tourassistant.coderoids.BaseActivity;
import com.tourassistant.coderoids.R;
import com.tourassistant.coderoids.adapters.InterestSelectionRecyclerView;
import com.tourassistant.coderoids.adapters.TripsDestinationAdapter;
import com.tourassistant.coderoids.appdb.AppDatabase;
import com.tourassistant.coderoids.helpers.AppHelper;
import com.tourassistant.coderoids.plantrip.tripdb.TripEntity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class EditTripFragment extends Fragment {
    TextInputEditText tvTripName, tvTextDesctiption, tvStartDate, tvEndDate;
    MaterialTextView etDestinaation;
    MaterialButton btnSave;
    Object id = "";
    ProgressDialog dialog;
    DatePickerDialog datePickerDialog;
    SwitchMaterial swTripState;
    TextView tripState;
    ImageButton ibDeleteTrip;
    int tag = -1;
    FirebaseUser users;
    List<DocumentSnapshot> interests;
    RecyclerView interstsGrid;
    LinearLayoutManager llm;
    private boolean rowState[] = new boolean[0];
    JSONArray intrestArray = new JSONArray();
    private static int AUTOCOMPLETE_REQUEST_CODE = 1;
    RecyclerView rvDestinations;
    LinearLayoutManager destinationsMan;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        users = FirebaseAuth.getInstance().getCurrentUser();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_trip, container, false);
        datePickerDialog = new DatePickerDialog(getActivity());
        initializeView(view);
        return view;
    }

    private void initializeView(View view) {
        tvTripName = view.findViewById(R.id.et_trip_name);
        tvTextDesctiption = view.findViewById(R.id.et_trip_description);
        etDestinaation = view.findViewById(R.id.et_destination);
        tvStartDate = view.findViewById(R.id.et_start_date);
        swTripState = view.findViewById(R.id.sw_trip_state);
        tripState = view.findViewById(R.id.trip_state);
        TextInputLayout tvInLayout = view.findViewById(R.id.tl_starte_date);
        TextInputLayout tvEndDateL = view.findViewById(R.id.tl_end_date);
        tvEndDate = view.findViewById(R.id.et_end_date);
        ibDeleteTrip = view.findViewById(R.id.delete_trip);
        btnSave = view.findViewById(R.id.btn_save);
        interstsGrid = view.findViewById(R.id.interests);
        rvDestinations = view.findViewById(R.id.rv_destinations);
        llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.HORIZONTAL);

        destinationsMan = new LinearLayoutManager(getActivity());
        destinationsMan.setOrientation(LinearLayoutManager.HORIZONTAL);
        tvInLayout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tag = 0;
                datePickerDialog.show();
            }
        });

        tvEndDateL.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tag = 1;
                datePickerDialog.show();
            }
        });

        datePickerDialog.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month + 1;
                String months = "";
                if (month < 10) {
                    months = "0" + month;
                } else
                    months = month + "";
                if (tag == 0) {
                    tvStartDate.setText(dayOfMonth + "-" + months + "-" + year);
                } else if (tag == 1) {
                    tvEndDate.setText(dayOfMonth + "-" + months + "-" + year);
                }
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTripDetails();
            }
        });

        ibDeleteTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        TripEntity tripEntity = new TripEntity();
                        tripEntity.setId((Integer) id);
                        AppDatabase.getAppDatabase(getActivity()).tripDao().deleteTrip(tripEntity);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog.setMessage("Deleting...");
                                dialog.setIndeterminate(true);
                                dialog.show();
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        //Do something after 100ms
                                        dialog.dismiss();
                                        Navigation.findNavController(requireView()).navigate(R.id.tripsFragment);

                                    }
                                }, 2000);

                            }
                        });

                    }

                });
            }
        });
        dialog = new ProgressDialog(getActivity());
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    TripEntity tripEntity = AppHelper.tripEntityList;
                    if(tripEntity.getTripTags() != null && !tripEntity.getTripTags().matches("")){
                        intrestArray = new JSONArray(tripEntity.getTripTags());
                    }
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvTripName.setText(tripEntity.getTripTitle());
                                if (tripEntity.getTripDescription() != null)
                                    tvTextDesctiption.setText(tripEntity.getTripDescription());
                                if (tripEntity.getStartDate() != null)
                                    tvStartDate.setText(tripEntity.getStartDate());
                                if (tripEntity.getEndDate() != null)
                                    tvEndDate.setText(tripEntity.getEndDate());
                                if (tripEntity.getDestination() != null) {
                                    etDestinaation.setText(tripEntity.getDestination());
                                }
                                if(tripEntity.getDestinationId() != null && !tripEntity.getDestinationId().matches("")){
//                                    if(tripEntity.getDestinationId().contains(",")) {
//                                        destinationIds = tripEntity.getDestinationId().split(",");
//                                    } else {
//                                        destinationIds = new String[1];
//                                        destinationIds[0] = tripEntity.getDestinationId();
//                                    }
//                                    if(tripEntity.getDestination().contains(",")) {
//                                        destinationNames = tripEntity.getDestination().split(",");
//                                    } else {
//                                        destinationNames = new String[1];
//                                        destinationNames [0]= tripEntity.getDestination();
//                                    }
                                    try {
                                        JSONArray jsonArray = new JSONArray(tripEntity.getDestinationId());
                                        TripsDestinationAdapter tripsDestinationAdapter = new TripsDestinationAdapter(getContext(),jsonArray);
                                        rvDestinations.setAdapter(tripsDestinationAdapter);
                                        rvDestinations.setLayoutManager(destinationsMan);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }
                            }
                        });

                    } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        });
        swTripState.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    tripState.setText("Your Trip is Private only You can view this");
                } else {
                    tripState.setText("Any one Can view Public Trips on Trip Assistant");
                }
            }
        });

        if (swTripState.isChecked()) {
            tripState.setText("Your Trip is Private only You can view this");
        } else {
            tripState.setText("Any one Can view Public Trips on Trip Assistant");
        }

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
                FirebaseUser users = FirebaseAuth.getInstance().getCurrentUser();
                String uid = users.getUid();
                rootRef.collection("Interests").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isComplete()) {
                            interests = task.getResult().getDocuments();
                            rowState = new boolean[interests.size()];
                            checkTripState();
                            InterestSelectionRecyclerView intrestsAdapter = new InterestSelectionRecyclerView(getActivity(), interests, rowState ,intrestArray);
                            interstsGrid.setAdapter(intrestsAdapter);
                            interstsGrid.setLayoutManager(llm);
                        }
                    }
                });
            }
        });

        etDestinaation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(requireView()).navigate(R.id.destinationsFragment);
            }
        });
    }

    private void checkTripState() {
        if (AppHelper.tripEntityList.getTripTags() != null && !AppHelper.tripEntityList.getTripTags().matches(""))
        {
            try {
                for (int i = 0; i < interests.size(); i++) {
                    JSONArray jsonArray = new JSONArray(AppHelper.tripEntityList.getTripTags());
                    for (int j = 0; j < jsonArray.length(); j++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(j);
                        DocumentSnapshot documentSnapshot = interests.get(i);
                        if(documentSnapshot.getId().toString().matches(jsonObject.getString("interestId"))){
                            rowState[i] = true;
                            break;
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveTripDetails() {
        if (!etDestinaation.getText().toString().matches("")) {
            dialog.setMessage("Updating...");
            dialog.setIndeterminate(true);
            dialog.show();
            TripEntity tripEntity = AppHelper.tripEntityList;
            tripEntity.setTripTitle(tvTripName.getText().toString());
            tripEntity.setTripDescription(tvTextDesctiption.getText().toString());
            tripEntity.setStartDate(tvStartDate.getText().toString());
            tripEntity.setEndDate(tvEndDate.getText().toString());
            tripEntity.setDestination(etDestinaation.getText().toString());
            tripEntity.setFirebaseUserId(users.getUid());
            tripEntity.setCreatorName(users.getDisplayName());
            if (AppHelper.tripEntityList.getJoinTripRequests() != null && !AppHelper.tripEntityList.getJoinTripRequests().matches("")) {
                tripEntity.setJoinTripRequests(AppHelper.tripEntityList.getJoinTripRequests());
            } else
                tripEntity.setJoinTripRequests("");
            if (swTripState.isChecked()) {
                tripState.setText("Your Trip is Private only You can view this");
                tripEntity.setIsPrivate("1");
            } else {
                tripState.setText("Any one Can view Public Trips on Trip Assistant");
                tripEntity.setIsPrivate("0");
            }

            tripEntity.setTripLocationTracking("0");
            BaseActivity.baseActivityInstance.editorLogin.putString("isTripInProgress", tripEntity.getTripLocationTracking()).apply();
            try {
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    tripEntity.setTripTags(intrestArray + "");
                    //AppDatabase.getAppDatabase(getActivity()).tripDao().updateTrip(tripEntity);
                    FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
                    String uid = users.getUid();
                    rootRef.collection("Trips").document(uid).collection("UserTrips").document(tripEntity.getFirebaseId()).set(tripEntity);
                    dialog.dismiss();
                    if (!swTripState.isChecked()) {
                        rootRef.collection("PublicTrips").document(tripEntity.getFirebaseId()).set(tripEntity);
                    }
                }
            });
        } else
            Toast.makeText(getContext(), "To Save Trip, Destination is Mandatory", Toast.LENGTH_SHORT).show();
    }


}