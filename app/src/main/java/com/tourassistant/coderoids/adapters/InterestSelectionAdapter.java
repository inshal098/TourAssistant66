package com.tourassistant.coderoids.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.tourassistant.coderoids.R;
import com.tourassistant.coderoids.helpers.AppHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class InterestSelectionAdapter extends BaseAdapter {
    Context context;
    JSONArray intestArr;
    LayoutInflater inflter;
    List<DocumentSnapshot> interests;
    boolean[] rowState;
    TextView interestCount;
    public InterestSelectionAdapter(Context applicationContext, List<DocumentSnapshot> interests, boolean[] rowState, TextView interestCount) {
        this.context = applicationContext;
        this.interests = interests;
        this.interestCount = interestCount;
        inflter = (LayoutInflater.from(applicationContext));
        this.rowState = rowState;
    }

    @Override
    public int getCount() {
        return interests.size();
    }

    @Override
    public Object getItem(int i) {
        try {
            return intestArr.get(i);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        view = inflter.inflate(R.layout.row_intrest, null); // inflate the layout
        Button materialTextView = view.findViewById(R.id.tv_intrest_tag);
        try {
            DocumentSnapshot documentSnapshot = interests.get(position);
            materialTextView.setText(documentSnapshot.getId());
            materialTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
                    rootRef.collection("Interests").document(documentSnapshot.getId()).collection(documentSnapshot.getId().toLowerCase()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
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
        return view;
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
        NestedInterestsAdapter nestedInterestsAdapter = new NestedInterestsAdapter(context, interest, interestRowState, AppHelper.interestUser);
        rvInterest.setAdapter(nestedInterestsAdapter);
        rvInterest.setLayoutManager(linearLayoutManager);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    for (int i = 0; i < interestRowState.length; i++) {
                        if (AppHelper.interestUser == null) {
                            AppHelper.interestUser = new JSONArray();
                        }
                        if (interestRowState[i]) {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("interestName", interest.get(i).getString("interestName"));
                            jsonObject.put("interestId", interest.get(i).getId());
                            jsonObject.put("parentTag", parentTag);
                            if (!AppHelper.interestUser.toString().contains(interest.get(i).getId())) {
                                AppHelper.interestUser.put(jsonObject);
                            }
                        } else {
                            if (AppHelper.interestUser.toString().contains(interest.get(i).getId())) {
                                for (int j = 0; j < AppHelper.interestUser.length(); j++) {
                                    JSONObject jsonObject = AppHelper.interestUser.getJSONObject(j);
                                    if (jsonObject.getString("interestId").matches(interest.get(i).getId())) {
                                        AppHelper.interestUser.remove(j);
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
                interestCount.setVisibility(View.VISIBLE);
                interestCount.setText("Total Preferences Selected : " + AppHelper.interestUser.length());
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }
}

