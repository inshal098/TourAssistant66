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
import com.tourassistant.coderoids.adapters.FollowRequestAdapter;
import com.tourassistant.coderoids.helpers.AppHelper;
import com.tourassistant.coderoids.interfaces.onClickListner;


public class FriendRequestFragment extends Fragment implements onClickListner {
    RecyclerView rvFollowRequest;
    MaterialTextView friendRequestTag;
    FollowRequestAdapter followRequestAdapter;
    LinearLayoutManager llm;
    boolean rowState[];
    onClickListner onClickListner;
    ProgressDialog progressDialog;

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
        friendRequestTag = view.findViewById(R.id.friend_request_tag);
        llm =new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.HORIZONTAL);
        populateFriendsList();
    }

    private void populateFriendsList() {
        if(AppHelper.friendRequests.size()>0){
            friendRequestTag.setVisibility(View.GONE);
            followRequestAdapter = new FollowRequestAdapter(getContext(),AppHelper.friendRequests,rowState,onClickListner,progressDialog);
            rvFollowRequest.setAdapter(followRequestAdapter);
            rvFollowRequest.setLayoutManager(llm);
        } else {
            followRequestAdapter.notifyDataSetChanged();
            friendRequestTag.setVisibility(View.VISIBLE);
        }
        if(progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
    }


    @Override
    public void onClick(int pos, DocumentSnapshot documentSnapshot) {
        AppHelper.friendRequests.remove(pos);
        populateFriendsList();
    }
}