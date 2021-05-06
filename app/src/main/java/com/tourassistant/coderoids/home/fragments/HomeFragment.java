package com.tourassistant.coderoids.home.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.tourassistant.PreDashBoardActivity;
import com.tourassistant.coderoids.R;
import com.tourassistant.coderoids.adapters.DestinationsAdapter;
import com.tourassistant.coderoids.adapters.InterestSelectionAdapter;
import com.tourassistant.coderoids.adapters.IntrestsAdapter;
import com.tourassistant.coderoids.appdb.AppDatabase;
import com.tourassistant.coderoids.helpers.AppHelper;
import com.tourassistant.coderoids.interfaces.RequestCompletionListener;
import com.tourassistant.coderoids.interfaces.onClickListner;
import com.tourassistant.coderoids.models.PlacesModel;
import com.tourassistant.coderoids.models.Profile;
import com.tourassistant.coderoids.plantrip.PlanTrip;
import com.tourassistant.coderoids.plantrip.tripdb.TripEntity;
import com.tourassistant.coderoids.adapters.PublicTripsAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


public class HomeFragment extends Fragment implements RequestCompletionListener , onClickListner {
    TextView tvTripCount ,tvFiltered,tvAll ,prefs, tvNotice;
    public static HomeFragment instance;
    RecyclerView rvPublicTrips;
    LinearLayoutManager llm;
    ImageButton addIntrest;
    int state = 0;
    List<DocumentSnapshot> interests;
    JSONArray intrestArray = new JSONArray();
    private boolean rowState[] = new boolean[0];
    private boolean destinationState[] = new boolean[0];
    RequestCompletionListener requestCompletionListener;
    onClickListner onClickListner;
    RecyclerView  savedDestinations;
    LinearLayoutManager savedDestinationLLM;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        requestCompletionListener = (RequestCompletionListener) this;
        onClickListner = (onClickListner) this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_home, container, false);
        initializeView(view);
        return view;
    }

    private void initializeView(View view) {
        ExtendedFloatingActionButton floatingActionButton = view.findViewById(R.id.extended_fab);
        tvTripCount = view.findViewById(R.id.trips_count);
        tvNotice = view.findViewById(R.id.notice);
        addIntrest = view.findViewById(R.id.addIntrest);
        tvFiltered = view.findViewById(R.id.filterdPrefs);
        prefs = view.findViewById(R.id.prefs);
        tvAll = view.findViewById(R.id.alltrips);
        savedDestinations = view.findViewById(R.id.destinations_rv);
        MaterialCardView mCTrip = view.findViewById(R.id.card_trip);
        rvPublicTrips = view.findViewById(R.id.public_trips);
        llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);

        savedDestinationLLM = new LinearLayoutManager(getActivity());
        savedDestinationLLM.setOrientation(LinearLayoutManager.HORIZONTAL);
        if(AppHelper.currentProfileInstance == null){
            tvNotice.setText("Please Complete Your Profile,To Get Started");
            addIntrest.setVisibility(View.GONE);
        }
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), PlanTrip.class));
            }
        });

        mCTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(requireView()).navigate(R.id.tripsFragment);
            }
        });
        getAllTrips();
        tvFiltered.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(state != 0){
                    state = 0;
                    populateTrips();
                }
            }
        });

        tvAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(state == 0){
                    state = 1;
                    populateTrips();
                }
            }
        });

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
                rootRef.collection("Interests").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isComplete()) {
                            interests = task.getResult().getDocuments();
                            rowState = new boolean[interests.size()];
                            uncheckAll();
                        }
                    }
                });
            }
        });

        addIntrest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertDialog();
            }
        });
        if(AppHelper.interestUser != null && !AppHelper.interestUser.toString().matches("")){
            String interest = "";
            for(int i=0; i<AppHelper.interestUser.length();i++){
                try {
                    JSONObject jsonObject = AppHelper.interestUser.getJSONObject(i);
                    if(interest.matches(""))
                        interest = jsonObject.getString("interestName");
                    else
                        interest = interest +" | " + jsonObject.getString("interestName");
                    prefs.setText(interest);
                } catch (JSONException ex){
                    ex.printStackTrace();
                }

            }
        }
        fetchDestinations();
    }

    private void fetchDestinations() {
        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        rootRef.collection("Destinations").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isComplete()) {
                    List<DocumentSnapshot> placesNew = task.getResult().getDocuments();
                    ArrayList<PlacesModel> fetchedPlaces = new ArrayList<>();
                    for (int i = 0; i < placesNew.size(); i++) {
                        DocumentSnapshot documentSnapshot = placesNew.get(i);
                        fetchedPlaces.add(documentSnapshot.toObject(PlacesModel.class));
                    }
                    if (fetchedPlaces.size() > 0) {
                        destinationState = new boolean[fetchedPlaces.size()];
                        DestinationsAdapter destinatonAdapter = new DestinationsAdapter(getActivity(), fetchedPlaces, destinationState,"H");
                        savedDestinations.setAdapter(destinatonAdapter);
                        savedDestinations.setLayoutManager(savedDestinationLLM);
                    }

                }

            }
        });

    }
    private void uncheckAll() {
        for (int i = 0; i < rowState.length; i++) {
            rowState[i] = false;
        }
    }

    private void getAllTrips() {
        try {
            populateTrips();
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void populateTrips() {
        List<DocumentSnapshot> array = new ArrayList<>();
        if(state ==0){
            array = AppHelper.filteredTrips;
            tvFiltered.setBackground(ContextCompat.getDrawable(getContext(),R.drawable.cell_filled));
            tvFiltered.setTextColor(ContextCompat.getColor(getContext(), R.color.white));

            tvAll.setBackground(ContextCompat.getDrawable(getContext(),R.drawable.cell));
            tvAll.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
        } else {
            array = AppHelper.allTrips;
            tvAll.setBackground(ContextCompat.getDrawable(getContext(),R.drawable.cell_filled));
            tvAll.setTextColor(ContextCompat.getColor(getContext(), R.color.white));

            tvFiltered.setBackground(ContextCompat.getDrawable(getContext(),R.drawable.cell));
            tvFiltered.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
        }
        if(array != null && array.size()>0) {
            PublicTripsAdapter publicTripsAdapter = new PublicTripsAdapter(getActivity(), array ,onClickListner);
            rvPublicTrips.setAdapter(publicTripsAdapter);
            rvPublicTrips.setLayoutManager(llm);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getAllTrips();
    }

    private void getUpdatedRowState() {
        try {
            for (int i = 0; i < interests.size(); i++) {
                String id = interests.get(i).getId();
                for (int j = 0; j < intrestArray.length(); j++) {
                    if (intrestArray.getJSONObject(j).getString("interestId").matches(id)) {
                        rowState[i] = true;
                        break;
                    }
                }
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }


    private void showAlertDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.view_interest, null);
        dialogBuilder.setView(dialogView);
        AlertDialog alertDialog = dialogBuilder.create();
        GridView interstsGrid = dialogView.findViewById(R.id.interests);
        Button btnSave = dialogView.findViewById(R.id.btnSave);
        TextView interestCount = dialogView.findViewById(R.id.interestCount);
        if (intrestArray.length() > 0) {
            getUpdatedRowState();
        }

        InterestSelectionAdapter interestsAdapterSelection = new InterestSelectionAdapter(getActivity(), interests, rowState ,interestCount);
        interstsGrid.setAdapter(interestsAdapterSelection);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    intrestArray = AppHelper.interestUser;
                    FilterPublicTrips filterPublicTrips = new FilterPublicTrips(getContext(), AppHelper.allTrips, requestCompletionListener);
                    AppHelper.filteredTrips = new ArrayList<>();
                    filterPublicTrips.filteredTrips(intrestArray);
                    FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
                    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                    DocumentReference uidRef = rootRef.collection("Users").document(firebaseUser.getUid());
                    uidRef.update("interests",intrestArray+"");
                    alertDialog.dismiss();
                    getAllTrips();
                    manageUserPreferences();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        });
        alertDialog.show();
    }

    private void manageUserPreferences(){
        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        rootRef.collection("Users").document(AppHelper.currentProfileInstance.getUserId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                try {
                    AppHelper.currentProfileInstance = documentSnapshot.toObject(Profile.class);
                    if(AppHelper.currentProfileInstance != null)
                        AppHelper.currentProfileInstance.setUserId(documentSnapshot.getId());
                    if (AppHelper.currentProfileInstance.getInterests() != null && !AppHelper.currentProfileInstance.getInterests().matches(""))
                        AppHelper.interestUser = new JSONArray(AppHelper.currentProfileInstance.getInterests());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onListFilteredCompletion(boolean status) {
        getAllTrips();
    }

    @Override
    public void onAllUsersCompletion(boolean status) {

    }

    @Override
    public void onClick(int pos,final DocumentSnapshot currentSnap) {
        ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Sending Request To Admin Now, Please Wait...");
        progressDialog.setIndeterminate(true);
        progressDialog.show();
        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DocumentReference uidRef = rootRef.collection("Trips").document(currentSnap.getString("firebaseUserId")).collection("UserTrips").document(currentSnap.getString("firebaseId"));
        DocumentReference uidRefPublic = rootRef.collection("PublicTrips").document(currentSnap.getString("firebaseId"));

        String joinTripRequests = currentSnap.getString("joinTripRequests");
        String userId = AppHelper.currentProfileInstance.getUserId();
        try {
            JSONArray tripRequestsArr = new JSONArray();
            JSONArray tripRequestsArrToUpdate = new JSONArray();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("userId",userId);
            if(joinTripRequests ==  null || joinTripRequests ==  "null" || joinTripRequests.matches("")|| joinTripRequests.matches("null")){
                tripRequestsArr.put(jsonObject);
            } else {
                tripRequestsArr = new JSONArray(joinTripRequests);
                tripRequestsArrToUpdate = new JSONArray(joinTripRequests);
                if(tripRequestsArr.length()>0) {
                    for (int i = 0; i < tripRequestsArr.length(); i++) {
                        JSONObject jsonObject1 = tripRequestsArr.getJSONObject(i);
                        if (!jsonObject1.getString("userId").matches(jsonObject.getString("userId"))) {
                            tripRequestsArr.put(jsonObject);
                        }
                    }
                } else {
                    tripRequestsArr.put(jsonObject);
                }
            }
            if(tripRequestsArrToUpdate.length() < tripRequestsArr.length()) {
                uidRef.update("joinTripRequests", tripRequestsArr + "");
                uidRefPublic.update("joinTripRequests", tripRequestsArr + "");
                rootRef.collection("PublicTrips").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isComplete()) {
                            List<DocumentSnapshot> publicTrips = task.getResult().getDocuments();
                            FilterPublicTrips filterPublicTrips = new FilterPublicTrips(getContext(), publicTrips, requestCompletionListener);
                            AppHelper.filteredTrips = new ArrayList<>();
                            filterPublicTrips.filteredTrips();
                            getAllTrips();
                            progressDialog.dismiss();

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        requestCompletionListener.onListFilteredCompletion(false);
                        progressDialog.dismiss();
                    }
                });
            } else {
                progressDialog.dismiss();
            }
        } catch (JSONException ex){
            ex.printStackTrace();
        }

    }
}