package com.tourassistant.coderoids.home.fragments;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.tourassistant.coderoids.R;
import com.tourassistant.coderoids.adapters.DestinationsAdapter;
import com.tourassistant.coderoids.adapters.InterestSelectionRecyclerView;
import com.tourassistant.coderoids.helpers.AppHelper;
import com.tourassistant.coderoids.models.PlacesModel;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class EditDestinationFragment extends Fragment {
    RecyclerView interstsGrid;
    List<DocumentSnapshot> interests;
    private boolean rowState[] = new boolean[0];
    LinearLayoutManager llm;
    TextInputEditText etDestinationName, etDestinationAddress;
    Button btnSave;
    JSONArray destIntrest = new JSONArray();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_destination, container, false);
        intializeViews(view);
        return view;
    }

    private void intializeViews(View view) {
        interstsGrid = view.findViewById(R.id.interests);
        etDestinationAddress = view.findViewById(R.id.tl_address_des_);
        etDestinationName = view.findViewById(R.id.dest_name_);
        btnSave = view.findViewById(R.id.btn_save);
        llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.HORIZONTAL);
        fetchIntrest();
    }


    private void fetchIntrest() {
        try {
            if (AppHelper.editDestModel != null && !AppHelper.editDestModel.getTripTags().matches("")) {
                destIntrest = new JSONArray(AppHelper.editDestModel.getTripTags());
            }
            etDestinationName.setText(AppHelper.editDestModel.getDestinationName());
            etDestinationAddress.setText(AppHelper.editDestModel.getDestinationAddress());
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
                                // checkTripState();
                                InterestSelectionRecyclerView intrestsAdapter = new InterestSelectionRecyclerView(getActivity(), interests, rowState, destIntrest);
                                interstsGrid.setAdapter(intrestsAdapter);
                                interstsGrid.setLayoutManager(llm);
                            }
                        }
                    });
                }
            });

            btnSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
                    DocumentReference reference = rootRef.collection("Destinations").document(AppHelper.editDestModel.getDestinationId());
                    reference.update("tripTags", destIntrest+"");
                }
            });
        } catch (JSONException ex) {
            ex.printStackTrace();
            FirebaseCrashlytics.getInstance().recordException(ex);
        }

    }
}