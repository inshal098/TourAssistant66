package com.tourassistant.coderoids.home.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.tourassistant.coderoids.R;
import com.tourassistant.coderoids.adapters.FriendsAdapter;
import com.tourassistant.coderoids.adapters.SuggestedFriendsAdapter;
import com.tourassistant.coderoids.helpers.AppHelper;
import com.tourassistant.coderoids.interfaces.onClickListner;

import java.util.ArrayList;
import java.util.List;

public class InviteFragment extends Fragment  implements onClickListner {
    RecyclerView rvInvite ,rvFriends;
    MaterialTextView friendRequestTag ,invitationTag ,friendsTag;
    LinearLayoutManager rvMang2,rvMang;
    onClickListner onClickListner;
    ProgressDialog progressDialog;
    SuggestedFriendsAdapter suggestedFriendsAdapter;
    FriendsAdapter friendsAdapter;
    boolean rowSate[];
    public static List<DocumentSnapshot> usersObj = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progressDialog = new ProgressDialog(getActivity());
        onClickListner = (onClickListner) this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_invite, container, false);
        initializeView(view);
        return view;
    }

    private void initializeView(View view) {
        rvInvite = view.findViewById(R.id.suggestions);
        rvFriends = view.findViewById(R.id.friends_list);
        friendsTag = view.findViewById(R.id.friends_tag);
        friendRequestTag = view.findViewById(R.id.friend_request_tag);
        invitationTag = view.findViewById(R.id.invite_tag);
        rvMang = new LinearLayoutManager(getActivity());
        rvMang.setOrientation(LinearLayoutManager.HORIZONTAL);

        rvMang2 = new LinearLayoutManager(getActivity());
        rvMang2.setOrientation(LinearLayoutManager.HORIZONTAL);
        if(AppHelper.allFriends != null && AppHelper.allFriends.size() > 0) {
            usersObj = AppHelper.allUsers;
        }
        populateFriendsSuggestionList();
        populateFriendsList();
    }


    private void populateFriendsList() {
        if(usersObj != null && usersObj.size() > 0){
            rowSate = new boolean[AppHelper.allFriends.size()];
            manageTripState();
            friendsTag.setVisibility(View.VISIBLE);
            friendsAdapter = new FriendsAdapter(getContext(),usersObj,onClickListner,rowSate);
            rvFriends.setAdapter(friendsAdapter);
            rvFriends.setLayoutManager(rvMang2);
        } else {
            friendsTag.setVisibility(View.GONE);
        }
    }

    private void manageTripState() {
        if(AppHelper.tripEntityList != null){
            for(int i=0; i<AppHelper.allFriends.size();i++){
                DocumentSnapshot documentSnapshot = AppHelper.allFriends.get(i);
                if(AppHelper.tripEntityList.getFriends() !=  null && AppHelper.tripEntityList.getFriends().contains(documentSnapshot.getString("userFirestoreIdReceiver"))){
                    rowSate[i] = true;
                }
            }

        }
    }

    private void populateFriendsSuggestionList() {
        if(usersObj != null && usersObj.size()>0){
            invitationTag.setVisibility(View.VISIBLE);
            suggestedFriendsAdapter = new SuggestedFriendsAdapter(getContext(),usersObj,onClickListner,progressDialog);
            rvInvite.setAdapter(suggestedFriendsAdapter);
            rvInvite.setLayoutManager(rvMang);
        } else {
            if(suggestedFriendsAdapter != null)
                suggestedFriendsAdapter.notifyDataSetChanged();
            friendRequestTag.setVisibility(View.VISIBLE);
            invitationTag.setVisibility(View.GONE);
        }

        if(progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
    }

    @Override
    public void onClick(int pos, DocumentSnapshot documentSnapshot , String tag) {
        usersObj.remove(pos);
        populateFriendsSuggestionList();
    }
}