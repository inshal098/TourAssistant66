package com.tourassistant.coderoids.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.tourassistant.coderoids.R;
import com.tourassistant.coderoids.helpers.AppHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class InterestSelectionRecyclerView extends RecyclerView.Adapter<InterestSelectionRecyclerView.ViewHolder> {
    Context context;
    JSONArray intestArr;
    LayoutInflater inflter;
    List<DocumentSnapshot> interests;
    JSONArray intrestArray;
    boolean[] rowState;

    public InterestSelectionRecyclerView(Context applicationContext, List<DocumentSnapshot> interests, boolean[] rowState, JSONArray intrestArray) {
        this.context = applicationContext;
        this.interests = interests;
        inflter = (LayoutInflater.from(applicationContext));
        this.rowState = rowState;
        this.intrestArray = intrestArray;
    }

    @NonNull
    @Override
    public InterestSelectionRecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.row_intrest, viewGroup, false);
        return new InterestSelectionRecyclerView.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final InterestSelectionRecyclerView.ViewHolder viewHolder, int position) {
        position = viewHolder.getAdapterPosition();
        try {
            try {
                DocumentSnapshot documentSnapshot = interests.get(position);
                viewHolder.materialTextView.setText(documentSnapshot.getId());
                int finalPosition = position;
                viewHolder.materialTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
                        rootRef.collection("Interests").document(documentSnapshot.getId()).collection(documentSnapshot.getId()
                                .toLowerCase())
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isComplete()) {
                                            List<DocumentSnapshot> interest = task.getResult().getDocuments();
                                            showAlertDialog((Activity) context, interest, documentSnapshot.getId());
                                        }
                                    }
                                });
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return interests.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        Button materialTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            materialTextView = itemView.findViewById(R.id.tv_intrest_tag);
        }
    }

    private void showAlertDialog(Activity context, List<DocumentSnapshot> interest, String parentTag) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = context.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.interest_listing, null);
        dialogBuilder.setView(dialogView);
        AlertDialog alertDialog = dialogBuilder.create();
        RecyclerView rvInterest = dialogView.findViewById(R.id.rv_interest_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        Button btnSave = dialogView.findViewById(R.id.btnSave);
        boolean[] interestRowState = new boolean[interest.size()];
        NestedInterestsAdapter nestedInterestsAdapter = new NestedInterestsAdapter(context, interest, interestRowState, intrestArray);
        rvInterest.setAdapter(nestedInterestsAdapter);
        rvInterest.setLayoutManager(linearLayoutManager);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    for (int i = 0; i < interestRowState.length; i++) {
                        if (intrestArray == null) {
                            intrestArray = new JSONArray();
                        }
                        if (interestRowState[i]) {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("interestName", interest.get(i).getString("interestName"));
                            jsonObject.put("interestId", interest.get(i).getId());
                            jsonObject.put("parentTag", parentTag);
                            if (!intrestArray.toString().contains(interest.get(i).getId())) {
                                intrestArray.put(jsonObject);
                            }
                        } else {
                            if (intrestArray.toString().contains(interest.get(i).getId())) {
                                for (int j = 0; j < intrestArray.length(); j++) {
                                    JSONObject jsonObject = intrestArray.getJSONObject(j);
                                    if (jsonObject.getString("interestId").matches(interest.get(i).getId())) {
                                        intrestArray.remove(j);
                                        i = 0;
                                        break;
                                    }
                                }
                            }

                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }
}

