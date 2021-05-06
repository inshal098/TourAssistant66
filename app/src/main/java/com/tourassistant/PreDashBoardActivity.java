package com.tourassistant;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.tourassistant.coderoids.R;
import com.tourassistant.coderoids.helpers.AppHelper;
import com.tourassistant.coderoids.home.DashboardActivity;
import com.tourassistant.coderoids.home.fragments.FilterPublicTrips;
import com.tourassistant.coderoids.interfaces.RequestCompletionListener;
import com.tourassistant.coderoids.models.Profile;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class PreDashBoardActivity extends AppCompatActivity implements RequestCompletionListener {
    ProgressDialog progressDialog;
    RequestCompletionListener requestCompletionListener;
    FirebaseFirestore rootRef;
    FirebaseUser users;
    Button error;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_dash_board);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        rootRef = FirebaseFirestore.getInstance();
        users = FirebaseAuth.getInstance().getCurrentUser();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please Wait While We Load your Content...");
        progressDialog.setIndeterminate(true);
        progressDialog.show();
        error = findViewById(R.id.error);
        requestCompletionListener = (RequestCompletionListener) this;
        error.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               handleState();
            }
        });
        handleState();
    }

    private void handleState(){
        if(AppHelper.isNetworkAvailable(getApplicationContext()))
            manageUserPreferences();
        else {
            progressDialog.dismiss();
            error.setText("No Internet Available, Please Connect And Retry");
        }
    }

    private void manageUserPreferences() {
        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();

        rootRef.collection("Users").document(users.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                try {
                    AppHelper.currentProfileInstance = documentSnapshot.toObject(Profile.class);
                    if (AppHelper.currentProfileInstance != null)
                        AppHelper.currentProfileInstance.setUserId(documentSnapshot.getId());
                    if (AppHelper.currentProfileInstance.getInterests() != null && !AppHelper.currentProfileInstance.getInterests().matches(""))
                        AppHelper.interestUser = new JSONArray(AppHelper.currentProfileInstance.getInterests());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        rootRef.collection("PublicTrips").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isComplete()) {
                    List<DocumentSnapshot> publicTrips = task.getResult().getDocuments();
                    FilterPublicTrips filterPublicTrips = new FilterPublicTrips(PreDashBoardActivity.this, publicTrips, requestCompletionListener);
                    AppHelper.filteredTrips = new ArrayList<>();
                    filterPublicTrips.filteredTrips();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                requestCompletionListener.onListFilteredCompletion(false);
            }
        });
    }

    @Override
    public void onListFilteredCompletion(boolean status) {
        manageFriends();
    }

    private void manageFriends() {
        rootRef.collection("Users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isComplete()) {
                    List<DocumentSnapshot> allUsers = task.getResult().getDocuments();
                    FirebaseUser users = FirebaseAuth.getInstance().getCurrentUser();
                    AppHelper.allUsers = new ArrayList<>();
                    for (int i = 0; i < allUsers.size(); i++) {
                        DocumentSnapshot documentSnapshot = allUsers.get(i);
                        if (!users.getUid().matches(documentSnapshot.getId())) {
                            AppHelper.allUsers.add(documentSnapshot);
                            requestCompletionListener.onAllUsersCompletion(true);
                        }
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onAllUsersCompletion(boolean status) {
        if (status) {
            rootRef.collection("Users").document(users.getUid()).collection("FriendRequestSent").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isComplete()) {
                        List<DocumentSnapshot> allSentRequest = task.getResult().getDocuments();
                        List<DocumentSnapshot> newAllUser = new ArrayList<>();
                        for (int i = 0; i < allSentRequest.size(); i++) {
                            DocumentSnapshot documentSnapshot = allSentRequest.get(i);
                            String userId = documentSnapshot.getString("userFirestoreIdReceiver");
                            if (userId != null) {
                                for (int j = 0; j < AppHelper.allUsers.size(); j++) {
                                    DocumentSnapshot documentSnapshot2 = AppHelper.allUsers.get(j);
                                    if (documentSnapshot2.getId() != null) {
                                        String usersId = documentSnapshot2.getId();
                                        if (!userId.matches(usersId)) {
                                            newAllUser.add(documentSnapshot2);
                                        }
                                    }
                                }
                            }

                        }
                        if (newAllUser.size() > 0) {
                            AppHelper.allUsers = newAllUser;
                        }
                        requestCompletionListener.onAllUsersCompletion(false);
                    }
                }
            });
        } else {
            if (AppHelper.currentProfileInstance != null) {
                rootRef.collection("Users").document(users.getUid()).collection("Friends").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isComplete()) {
                            List<DocumentSnapshot> friendsList = task.getResult().getDocuments();
                            AppHelper.allFriends = friendsList;
                            List<DocumentSnapshot> newAllUser = new ArrayList<>();
                            for (int i = 0; i < friendsList.size(); i++) {
                                DocumentSnapshot documentSnapshot = friendsList.get(i);
                                String userId = documentSnapshot.getString("userFirestoreIdReceiver");
                                if (userId.matches(AppHelper.currentProfileInstance.getUserId())) {
                                    userId = documentSnapshot.getString("userFirestoreIdSender");
                                }
                                for (int j = 0; j < AppHelper.allUsers.size(); j++) {
                                    DocumentSnapshot documentSnapshot2 = AppHelper.allUsers.get(j);
                                    String usersId = documentSnapshot2.getId();
                                    if (!userId.matches(usersId)) {
                                        newAllUser.add(documentSnapshot2);
                                    }
                                }

                            }
                            DocumentReference uidRef4 = rootRef.collection("Users").document(AppHelper.currentProfileInstance.getUserId());
                            uidRef4.update("followers", AppHelper.allFriends.size() + "");
                            uidRef4.update("following", AppHelper.allFriends.size() + "");
                            if (newAllUser.size() > 0) {
                                AppHelper.allUsers = newAllUser;
                            }
                            progressDialog.dismiss();
                            startActivity(new Intent(PreDashBoardActivity.this, DashboardActivity.class));
                            finish();
                        }
                    }
                });
            } else {
                progressDialog.dismiss();
                startActivity(new Intent(PreDashBoardActivity.this, DashboardActivity.class));
                finish();
            }
        }

    }
}