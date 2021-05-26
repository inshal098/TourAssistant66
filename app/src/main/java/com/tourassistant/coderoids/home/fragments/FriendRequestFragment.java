package com.tourassistant.coderoids.home.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.tourassistant.coderoids.R;
import com.tourassistant.coderoids.adapters.FollowRequestAdapter;
import com.tourassistant.coderoids.adapters.FriendsListingAdapter;
import com.tourassistant.coderoids.adapters.SuggestedFriendsAdapter;
import com.tourassistant.coderoids.helpers.AppHelper;
import com.tourassistant.coderoids.home.DashboardActivity;
import com.tourassistant.coderoids.interfaces.onClickListner;

import java.util.ArrayList;
import java.util.List;


public class FriendRequestFragment extends Fragment implements onClickListner {
    RecyclerView rvFollowRequest, rvFriendsList, rvUsers;
    MaterialTextView friendRequestTag;
    FollowRequestAdapter followRequestAdapter;
    LinearLayoutManager llm, friendsLayout, rvManag;
    boolean rowState[];
    onClickListner onClickListner;
    ProgressDialog progressDialog;
    ArrayList<DocumentSnapshot> friendsList = new ArrayList<>();
    List<DocumentSnapshot> usersObj;
    SuggestedFriendsAdapter suggestedFriendsAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onClickListner = (onClickListner) this;
        progressDialog = new ProgressDialog(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend_request, container, false);
        initializeView(view);
        return view;
    }

    private void initializeView(View view) {
        rvFollowRequest = view.findViewById(R.id.rv_follow_request);
        rvUsers = view.findViewById(R.id.user_listing);
        rvFriendsList = view.findViewById(R.id.friends_listing);
        friendRequestTag = view.findViewById(R.id.friend_request_tag);
        llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.HORIZONTAL);

        rvManag = new LinearLayoutManager(getActivity());
        rvManag.setOrientation(LinearLayoutManager.HORIZONTAL);

        friendsLayout = new LinearLayoutManager(getActivity());
        friendsLayout.setOrientation(LinearLayoutManager.HORIZONTAL);
        fetchAllUsers();
        populateFriendsList();
        populateFriends();
        populateUsers();
    }

    private void populateUsers() {
        if (AppHelper.allUsers != null && AppHelper.allUsers.size() > 0) {
            List<DocumentSnapshot> users = filterUsers();
            suggestedFriendsAdapter = new SuggestedFriendsAdapter(getContext(), users, onClickListner, progressDialog);
            rvUsers.setAdapter(suggestedFriendsAdapter);
            rvUsers.setLayoutManager(rvManag);
        }
    }

    private void populateFriendsSuggestionList() {
        if(usersObj != null && usersObj.size()>0){
            suggestedFriendsAdapter = new SuggestedFriendsAdapter(getContext(),usersObj,onClickListner,progressDialog);
            rvUsers.setAdapter(suggestedFriendsAdapter);
            rvUsers.setLayoutManager(rvManag);
        } else {
            if(suggestedFriendsAdapter != null)
                suggestedFriendsAdapter.notifyDataSetChanged();
        }

        if(progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
    }

    private List<DocumentSnapshot> filterUsers() {
        usersObj =  AppHelper.allUsers;
        String addedId = "";
        if (AppHelper.allFriends != null) {
            for (int k = 0; k < AppHelper.allFriends.size(); k++) {
                DocumentSnapshot documentSnapshot = AppHelper.allFriends.get(k);
                String user1 = documentSnapshot.getString("userFirestoreIdReceiver");
                String user2 = documentSnapshot.getString("userFirestoreIdSender");
                String reciverId = "";
                if (AppHelper.currentProfileInstance.getUserId().matches(user1)) {
                    reciverId = user2;
                } else
                    reciverId = user1;
                for (int i = 0; i < usersObj.size(); i++) {
                    String userId = usersObj.get(i).getId();
                    if(userId.matches(reciverId))
                        usersObj.remove(i);
                }
            }
        }
        return usersObj;
    }


    private void populateFriends() {
        if (AppHelper.allFriends != null && AppHelper.allFriends.size() > 0) {
            FriendsListingAdapter friendsListingAdapter = new FriendsListingAdapter(getContext(), AppHelper.allFriends);
            rvFriendsList.setAdapter(friendsListingAdapter);
            rvFriendsList.setLayoutManager(friendsLayout);
        }
    }

    private void fetchAllUsers() {
        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        FirebaseUser users = FirebaseAuth.getInstance().getCurrentUser();
        String uid = users.getUid();
        rootRef.collection("Users").document(uid).collection("FriendRequestsReceived").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isComplete()) {
                    List<DocumentSnapshot> friendRequest = task.getResult().getDocuments();
                    if (friendRequest.size() > 0) {
                        AppHelper.friendRequests = friendRequest;
                    } else
                        Toast.makeText(getActivity(), "No New Friend Requests", Toast.LENGTH_SHORT).show();

//                    rootRef.collection("Users").document(uid).collection("Friends").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                        @Override
//                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                            if (task.isComplete()) {
//                                List<DocumentSnapshot> friends = task.getResult().getDocuments();
//                                friendsList = friendsList;
//                            }
//                        }
//                    });
                }
            }
        });
    }

    private void populateFriendsList() {
        if (AppHelper.friendRequests.size() > 0) {
            friendRequestTag.setVisibility(View.GONE);
            followRequestAdapter = new FollowRequestAdapter(getContext(), AppHelper.friendRequests, rowState, onClickListner, progressDialog);
            rvFollowRequest.setAdapter(followRequestAdapter);
            rvFollowRequest.setLayoutManager(llm);
        } else if (followRequestAdapter != null) {
            followRequestAdapter.notifyDataSetChanged();
            friendRequestTag.setVisibility(View.VISIBLE);
        }
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
    }


    @Override
    public void onClick(int pos, DocumentSnapshot documentSnapshot , String tag) {
        if (tag.matches("SF")) {
            usersObj.remove(pos);
            populateFriendsSuggestionList();
        } else {
            AppHelper.friendRequests.remove(pos);
            populateFriendsList();
        }
    }
}