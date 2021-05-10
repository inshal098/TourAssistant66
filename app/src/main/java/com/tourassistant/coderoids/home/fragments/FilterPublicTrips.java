package com.tourassistant.coderoids.home.fragments;

import android.content.Context;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tourassistant.coderoids.helpers.AppHelper;
import com.tourassistant.coderoids.interfaces.RequestCompletionListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FilterPublicTrips {
    Context context;
    List<DocumentSnapshot> publicTrips;
    List<DocumentSnapshot> filteredArray = new ArrayList<>();
    JSONArray usersInterest;
    RequestCompletionListener requestCompletionListener;

    public FilterPublicTrips(Context context, List<DocumentSnapshot> publicTrips, RequestCompletionListener requestCompletionListener) {
        this.context = context;
        this.publicTrips = publicTrips;
        this.requestCompletionListener = requestCompletionListener;
    }

    public void filteredTrips(JSONArray intrestArray) {
        try {
            JSONArray profileList = intrestArray;
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            AppHelper.allTrips = publicTrips;
            if (profileList != null) {
                usersInterest = profileList;
                for (int i = 0; i < publicTrips.size(); i++) {
                    DocumentSnapshot documentSnapshot1 = publicTrips.get(i);
                    if (documentSnapshot1.getString("tripTags") != null && !documentSnapshot1.getString("tripTags").matches("null") && !documentSnapshot1.getString("tripTags").matches("")) {
                        JSONArray tripInterestTag = new JSONArray(documentSnapshot1.getString("tripTags"));
                        for (int k = 0; k < tripInterestTag.length(); k++) {
                            boolean isMatched = false;
                            JSONObject tripJob = tripInterestTag.getJSONObject(k);
                            for (int j = 0; j < usersInterest.length(); j++) {
                                JSONObject userInterest = usersInterest.getJSONObject(j);
                                if (userInterest.getString("interestId").matches(tripJob.getString("interestId"))
                                        && !documentSnapshot1.getString("firebaseUserId").matches(firebaseUser.getUid())) {
                                    filteredArray.add(documentSnapshot1);
                                    isMatched = true;
                                    break;
                                }
                            }
                            if (isMatched)
                                break;
                        }
                    }

                }

                if (filteredArray.size() > 0) {
                    AppHelper.filteredTrips = filteredArray;
                    requestCompletionListener.onListFilteredCompletion(true);
                }

            } else
                requestCompletionListener.onListFilteredCompletion(false);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }


    public void filteredTrips() {
        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        FirebaseUser users = FirebaseAuth.getInstance().getCurrentUser();
        rootRef.collection("Users").document(users.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                try {
                    Map<String, Object> profileList = documentSnapshot.getData();
                    AppHelper.allTrips = publicTrips;
                    if (profileList != null) {
                        if (profileList.containsKey("interests") && !profileList.get("interests").toString().matches("")) {
                            usersInterest = new JSONArray(profileList.get("interests").toString());
                            for (int i = 0; i < publicTrips.size(); i++) {
                                DocumentSnapshot documentSnapshot1 = publicTrips.get(i);
                                if (!documentSnapshot1.getString("firebaseUserId").matches(users.getUid())) {
                                    if (documentSnapshot1.getString("tripTags") != null && !documentSnapshot1.getString("tripTags").matches("")) {
                                        JSONArray tripInterestTag = new JSONArray(documentSnapshot1.getString("tripTags"));
                                        if (tripInterestTag.length() > 0) {
                                            for (int k = 0; k < tripInterestTag.length(); k++) {
                                                boolean isMatched = false;
                                                JSONObject tripJob = tripInterestTag.getJSONObject(k);
                                                for (int j = 0; j < usersInterest.length(); j++) {
                                                    JSONObject userInterest = usersInterest.getJSONObject(j);
                                                    if (userInterest.getString("interestId").matches(tripJob.getString("interestId"))) {
                                                        filteredArray.add(documentSnapshot1);
                                                        isMatched = true;
                                                        break;
                                                    }
                                                }
                                                if (isMatched)
                                                    break;
                                            }
                                        } else {
                                            requestCompletionListener.onListFilteredCompletion(false);
                                        }
                                    }
                                }
                            }

                            if (filteredArray.size() > 0) {
                                AppHelper.filteredTrips = filteredArray;
                                requestCompletionListener.onListFilteredCompletion(true);
                            } else
                                requestCompletionListener.onListFilteredCompletion(false);
                        } else
                            requestCompletionListener.onListFilteredCompletion(false);
                    } else
                        requestCompletionListener.onListFilteredCompletion(false);
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
}
